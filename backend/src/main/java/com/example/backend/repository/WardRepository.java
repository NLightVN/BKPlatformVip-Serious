package com.example.backend.repository;

import com.example.backend.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, String> {
    // Tìm tất cả phường xã thuộc 1 quận
    List<Ward> findAllByDistrict_Code(String districtCode);
}