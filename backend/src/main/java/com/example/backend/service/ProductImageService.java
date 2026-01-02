package com.example.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String preset) {
        try {
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "upload_preset", preset  // Fixed: correct parameter name
                    )
            );
            
            // Check if secure_url exists
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null) {
                throw new RuntimeException("Cloudinary did not return secure_url. Response: " + result);
            }
            
            return secureUrl.toString();
        } catch (Exception e) {
            throw new RuntimeException("Upload image failed: " + e.getMessage(), e);
        }
    }
    //OK
}

