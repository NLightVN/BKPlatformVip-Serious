package com.example.backend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductImageUploadRequest {
    MultipartFile file;

}
