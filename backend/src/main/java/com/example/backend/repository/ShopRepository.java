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

    // theo District CODE
    @Query("""
        SELECT s FROM Shop s
        WHERE s.address.ward.district.code = :districtCode
    """)
    List<Shop> findAllByDistrictCode(@Param("districtCode") String districtCode);

    // theo Province CODE
    @Query("""
        SELECT s FROM Shop s
        WHERE s.address.ward.district.province.code = :provinceCode
    """)
    List<Shop> findAllByProvinceCode(@Param("provinceCode") String provinceCode);

    // theo cả District + Province CODE
    @Query("""
        SELECT s FROM Shop s
        WHERE s.address.ward.district.code = :districtCode
          AND s.address.ward.district.province.code = :provinceCode
    """)
    List<Shop> findAllByDistrictAndProvinceCode(
            @Param("districtCode") String districtCode,
            @Param("provinceCode") String provinceCode
    );
}
