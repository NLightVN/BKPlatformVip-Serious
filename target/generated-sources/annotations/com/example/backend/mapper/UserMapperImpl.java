package com.example.backend.mapper;

import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.dto.response.PermissionResponse;
import com.example.backend.dto.response.RoleResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.AddressBook;
import com.example.backend.entity.Permission;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T15:55:47+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserCreationRequest user) {
        if ( user == null ) {
            return null;
        }

        User.UserBuilder user1 = User.builder();

        user1.username( user.getUsername() );
        user1.password( user.getPassword() );
        user1.fullname( user.getFullname() );
        user1.email( user.getEmail() );
        user1.address( addressDTOToAddressBook( user.getAddress() ) );

        return user1.build();
    }

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.userId( user.getUserId() );
        userResponse.username( user.getUsername() );
        userResponse.fullname( user.getFullname() );
        userResponse.email( user.getEmail() );
        userResponse.roles( roleSetToRoleResponseSet( user.getRoles() ) );
        userResponse.password( user.getPassword() );
        userResponse.createdDate( user.getCreatedDate() );
        userResponse.address( addressBookToAddressDTO( user.getAddress() ) );

        return userResponse.build();
    }

    @Override
    public void updateUser(User user, UserUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        user.setPassword( request.getPassword() );
        user.setFullname( request.getFullname() );
        user.setEmail( request.getEmail() );
        if ( request.getAddress() != null ) {
            if ( user.getAddress() == null ) {
                user.setAddress( AddressBook.builder().build() );
            }
            addressDTOToAddressBook1( request.getAddress(), user.getAddress() );
        }
        else {
            user.setAddress( null );
        }
    }

    protected AddressBook addressDTOToAddressBook(AddressDTO addressDTO) {
        if ( addressDTO == null ) {
            return null;
        }

        AddressBook.AddressBookBuilder addressBook = AddressBook.builder();

        addressBook.phone( addressDTO.getPhone() );
        addressBook.addressDetail( addressDTO.getAddressDetail() );
        addressBook.ward( addressDTO.getWard() );
        addressBook.district( addressDTO.getDistrict() );
        addressBook.province( addressDTO.getProvince() );
        addressBook.googleMapId( addressDTO.getGoogleMapId() );

        return addressBook.build();
    }

    protected PermissionResponse permissionToPermissionResponse(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        PermissionResponse.PermissionResponseBuilder permissionResponse = PermissionResponse.builder();

        permissionResponse.name( permission.getName() );
        permissionResponse.description( permission.getDescription() );

        return permissionResponse.build();
    }

    protected Set<PermissionResponse> permissionSetToPermissionResponseSet(Set<Permission> set) {
        if ( set == null ) {
            return null;
        }

        Set<PermissionResponse> set1 = LinkedHashSet.newLinkedHashSet( set.size() );
        for ( Permission permission : set ) {
            set1.add( permissionToPermissionResponse( permission ) );
        }

        return set1;
    }

    protected RoleResponse roleToRoleResponse(Role role) {
        if ( role == null ) {
            return null;
        }

        RoleResponse.RoleResponseBuilder roleResponse = RoleResponse.builder();

        roleResponse.name( role.getName() );
        roleResponse.description( role.getDescription() );
        roleResponse.permissions( permissionSetToPermissionResponseSet( role.getPermissions() ) );

        return roleResponse.build();
    }

    protected Set<RoleResponse> roleSetToRoleResponseSet(Set<Role> set) {
        if ( set == null ) {
            return null;
        }

        Set<RoleResponse> set1 = LinkedHashSet.newLinkedHashSet( set.size() );
        for ( Role role : set ) {
            set1.add( roleToRoleResponse( role ) );
        }

        return set1;
    }

    protected AddressDTO addressBookToAddressDTO(AddressBook addressBook) {
        if ( addressBook == null ) {
            return null;
        }

        AddressDTO.AddressDTOBuilder addressDTO = AddressDTO.builder();

        addressDTO.phone( addressBook.getPhone() );
        addressDTO.addressDetail( addressBook.getAddressDetail() );
        addressDTO.ward( addressBook.getWard() );
        addressDTO.district( addressBook.getDistrict() );
        addressDTO.province( addressBook.getProvince() );
        addressDTO.googleMapId( addressBook.getGoogleMapId() );

        return addressDTO.build();
    }

    protected void addressDTOToAddressBook1(AddressDTO addressDTO, AddressBook mappingTarget) {
        if ( addressDTO == null ) {
            return;
        }

        mappingTarget.setPhone( addressDTO.getPhone() );
        mappingTarget.setAddressDetail( addressDTO.getAddressDetail() );
        mappingTarget.setWard( addressDTO.getWard() );
        mappingTarget.setDistrict( addressDTO.getDistrict() );
        mappingTarget.setProvince( addressDTO.getProvince() );
        mappingTarget.setGoogleMapId( addressDTO.getGoogleMapId() );
    }
}
