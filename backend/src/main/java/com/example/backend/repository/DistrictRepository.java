package com.example.backend.repository;

import com.example.backend.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, String> {
    // Tìm tất cả quận huyện thuộc 1 tỉnh (để dropdown menu frontend)
    List<District> findAllByProvince_Code(String provinceCode);
}