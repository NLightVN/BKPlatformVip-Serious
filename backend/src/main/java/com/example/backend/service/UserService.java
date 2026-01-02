package com.example.backend.service;

import com.example.backend.constant.PredefinedRole;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.entity.User;
import com.example.backend.entity.Role;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static com.example.backend.util.SecurityUtil.requireAdmin;

@Service
@RequiredArgsConstructor
// tạo constructor chứa tất cả các field final
// spring sẽ tự động inject các class cần thiết nên ko cần @autowired
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
// chuyển tất cả các field thành private và final
@Slf4j
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (!request.getEmail().matches("^[A-Za-z0-9._%+-]+@(sis\\.)?hust\\.edu\\.vn$")) {
            throw new AppException(ErrorCode.INVALID_EMAIL);
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        user.setCreatedDate(LocalDateTime.now());
        return userMapper.toUserResponse(userRepository.save(user));
    }


    public List<UserResponse> getAllUser() {
        // chuyển list user lấy từ database thành 1 stream để dùng method map giúp thực hiện song song user ->
        // userMapper.toUserResponse(user) sau đó chuyển lại thành list
        requireAdmin();
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }


    public UserResponse getUserById(String userId) {
        requireAdmin();
        return userMapper.toUserResponse(
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST)));
    }


    public UserResponse updateUser(String userId, UserUpdateRequest request) {

        String currentUsername = SecurityUtil.getCurrentUsername();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        if (!user.getUsername().equals(currentUsername)
                && !SecurityUtil.hasRole("ADMIN")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        userMapper.updateUser(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null) {
            if (!SecurityUtil.hasRole("ADMIN")) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }


        return userMapper.toUserResponse(userRepository.save(user));
    }



    public UserResponse getMyInfor() {
        // SecurityContextHolder
        //   ↳ SecurityContext : đối tượng trung gian để lưu  Ai đang đăng nhập, Quyền của họ là gì
        //        ↳ Authentication: lưu principal, credentials, authoritizes, details, authenticated, name
        String name = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        return userMapper.toUserResponse(user);
    }


    public void deleteUser(String userId) {

        requireAdmin();
        userRepository.deleteById(userId);
    }
}
