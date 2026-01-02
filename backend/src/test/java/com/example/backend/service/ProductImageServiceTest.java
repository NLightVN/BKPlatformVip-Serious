package com.example.backend.service;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    Cloudinary cloudinary;

    @Mock
    MultipartFile multipartFile;

    @InjectMocks
    ProductImageService productImageService;

    @BeforeEach
    void setUp() {
        // Mock the uploader chain
        when(cloudinary.uploader()).thenReturn(mock(com.cloudinary.Uploader.class));
    }

    @Test
    void uploadImage_success() throws Exception {
        String preset = "product-preset";
        String expectedUrl = "https://cloudinary.com/image.jpg";
        byte[] fileBytes = "test image content".getBytes();

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", expectedUrl);

        com.cloudinary.Uploader uploader = mock(com.cloudinary.Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        String result = productImageService.uploadImage(multipartFile, preset);

        assertNotNull(result);
        assertEquals(expectedUrl, result);
        verify(cloudinary).uploader();
        verify(uploader).upload(any(byte[].class), any(Map.class));
    }

    @Test
    void uploadImage_uploadFails() throws Exception {
        String preset = "product-preset";
        byte[] fileBytes = "test image content".getBytes();

        com.cloudinary.Uploader uploader = mock(com.cloudinary.Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new RuntimeException("Upload failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productImageService.uploadImage(multipartFile, preset);
        });

        assertEquals("Upload image failed", exception.getMessage());
        verify(cloudinary).uploader();
        verify(uploader).upload(any(byte[].class), any(Map.class));
    }

    @Test
    void uploadImage_getBytesFails() throws Exception {
        String preset = "product-preset";

        when(multipartFile.getBytes()).thenThrow(new RuntimeException("Cannot read file"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productImageService.uploadImage(multipartFile, preset);
        });

        assertEquals("Upload image failed", exception.getMessage());
        verify(multipartFile).getBytes();
    }
}
