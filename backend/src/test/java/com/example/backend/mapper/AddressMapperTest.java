package com.example.backend.mapper;

import com.example.backend.dto.request.AddressDTO;
import com.example.backend.entity.AddressBook;
import com.example.backend.entity.Ward;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.WardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test cho AddressMapper - MapStruct mapper với custom logic
 * <p>
 * Note: AddressMapper là abstract class được MapStruct generate implementation
 * Test tập trung vào custom mapping logic (map method)
 */
@ExtendWith(MockitoExtension.class)
class AddressMapperTest {

    @Mock
    private WardRepository wardRepository;

    private AddressMapper addressMapper;

    private Ward testWard;

    @BeforeEach
    void setUp() {
        // Create instance of generated implementation
        addressMapper = new AddressMapperImpl();
        // Inject mock repository using reflection
        ReflectionTestUtils.setField(Objects.requireNonNull(addressMapper), "wardRepository", wardRepository);

        testWard = Ward.builder()
                .code("001")
                .fullName("Test Ward")
                .build();
    }

    @Test
    void map_validWardCode_returnsWard() {
        //setup
        when(wardRepository.findById("001")).thenReturn(Optional.of(testWard));

        //chay - Use reflection to call protected method
        Ward result = ReflectionTestUtils.invokeMethod(Objects.requireNonNull(addressMapper), "map", "001");
        //map là protected, không thể gọi trực tiếp từ test.
        //ReflectionTestUtils.invokeMethod cho phép gọi phương thức protected/private bằng reflection.
        //Ở đây nó gọi addressMapper.map("001") và trả về Ward kết quả.
        // Assert
        assertNotNull(result);
        assertEquals("001", result.getCode());
        assertEquals("Test Ward", result.getFullName());
        verify(wardRepository, times(1)).findById("001");
    }

    @Test
    void map_wardNotFound_throwsException() {
        //setup
        when(wardRepository.findById("999")).thenReturn(Optional.empty());

        //chay & Assert
        AppException exception = assertThrows(AppException.class, () ->
                ReflectionTestUtils.invokeMethod(Objects.requireNonNull(addressMapper), "map", "999"));

        assertEquals(ErrorCode.WARD_NOT_FOUND, exception.getErrorCode());
        verify(wardRepository, times(1)).findById("999");
    }

    @Test
    void map_nullWardCode_returnsNull() {
        //chay
        Ward result = ReflectionTestUtils.invokeMethod(Objects.requireNonNull(addressMapper), "map", (String) null);

        //check
        assertNull(result);
        verify(wardRepository, never()).findById(any(String.class));
    }

    @Test
    void map_blankWardCode_returnsNull() {
        //chay
        Ward result = ReflectionTestUtils.invokeMethod(Objects.requireNonNull(addressMapper), "map", "   ");

        //check
        assertNull(result);
        verify(wardRepository, never()).findById(any(String.class));
    }

    @Test
    void map_emptyWardCode_returnsNull() {
        //chay
        Ward result = ReflectionTestUtils.invokeMethod(Objects.requireNonNull(addressMapper), "map", "");

        //check
        assertNull(result);
        verify(wardRepository, never()).findById(any(String.class));
    }

    @Test
    void toDto_success() {
        //setup
        AddressBook addressBook = AddressBook.builder()
                .name("Test User")
                .phone("0123456789")
                .addressDetail("123 Test Street")
                .ward(testWard)
                .build();

        //chay
        AddressDTO dto = addressMapper.toDto(addressBook);

        //check
        assertNotNull(dto);
        assertEquals("Test User", dto.getName());
        assertEquals("0123456789", dto.getPhone());
        assertEquals("123 Test Street", dto.getAddressDetail());
        assertEquals("001", dto.getWardCode());
    }

    @Test
    void toDto_nullEntity_returnsNull() {
        //chay
        AddressDTO dto = addressMapper.toDto(null);

        //check
        assertNull(dto);
    }

    @Test
    void toEntity_success() {
        //setup
        AddressDTO dto = AddressDTO.builder()
                .name("Test User")
                .phone("0123456789")
                .addressDetail("123 Test Street")
                .wardCode("001")
                .build();

        when(wardRepository.findById("001")).thenReturn(Optional.of(testWard));

        //chay
        AddressBook entity = addressMapper.toEntity(dto);

        //check
        assertNotNull(entity);
        assertEquals("Test User", entity.getName());
        assertEquals("0123456789", entity.getPhone());
        assertEquals("123 Test Street", entity.getAddressDetail());
        assertNotNull(entity.getWard());
        assertEquals("001", entity.getWard().getCode());
        verify(wardRepository, times(1)).findById("001");
    }

    @Test
    void toEntity_wardNotFound_throwsException() {
        //setup
        AddressDTO dto = AddressDTO.builder()
                .wardCode("999")
                .build();

        when(wardRepository.findById("999")).thenReturn(Optional.empty());

        //chay & Assert
        AppException exception = assertThrows(AppException.class, () -> addressMapper.toEntity(dto));

        assertEquals(ErrorCode.WARD_NOT_FOUND, exception.getErrorCode());
        verify(wardRepository, times(1)).findById("999");
    }

    @Test
    void toEntity_nullWardCode_returnsEntityWithNullWard() {
        //setup
        AddressDTO dto = AddressDTO.builder()
                .name("Test User")
                .wardCode(null)
                .build();

        //chay
        AddressBook entity = addressMapper.toEntity(dto);

        //check
        assertNotNull(entity);
        assertNull(entity.getWard());
        verify(wardRepository, never()).findById(any(String.class));
    }
}
