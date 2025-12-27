package com.example.backend.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dlvfuktdv",
                "api_key", "178286218563568",
                "api_secret", "zSTJZj6bBJvPO7D_VJei3vAA4lA",
                "secure", true
        ));
    }
}
