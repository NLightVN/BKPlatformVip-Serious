package com.example.backend.repository;

import com.example.backend.entity.Shop;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, String> {

    List<Shop> findAllByOwner(User owner);

    // query dbrr theo logic shop -> address -> ward -> district -> province

    // Tìm theo Quận (District)
    @Query("SELECT s FROM Shop s WHERE s.address.ward.district.fullName LIKE %:district%")
    List<Shop> findAllByAddress_District(@Param("district") String district);

    // Tìm theo Tỉnh (Province)
    @Query("SELECT s FROM Shop s WHERE s.address.ward.district.province.fullName LIKE %:province%")
    List<Shop> findAllByAddress_Province(@Param("province") String province);

    // Tìm theo cả Quận và Tỉnh
    @Query("SELECT s FROM Shop s WHERE s.address.ward.district.fullName LIKE %:district% AND s.address.ward.district.province.fullName LIKE %:province%")
    List<Shop> findAllByAddress_DistrictAndAddress_Province(@Param("district") String district, @Param("province") String province);
}