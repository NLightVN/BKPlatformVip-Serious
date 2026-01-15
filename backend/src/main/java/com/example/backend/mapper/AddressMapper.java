package com.example.backend.mapper;

import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.response.DistrictResponse;
import com.example.backend.dto.response.ProvinceResponse;
import com.example.backend.dto.response.WardResponse;
import com.example.backend.entity.AddressBook;
import com.example.backend.entity.District;
import com.example.backend.entity.Province;
import com.example.backend.entity.Ward;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.WardRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class AddressMapper {

    @Autowired
    protected WardRepository wardRepository;

    @Mapping(target = "wardCode", source = "ward.code") // Map code để frontend dùng logic
    @Mapping(target = "ward", source = "ward", qualifiedByName = "mapWardToResponse")
    @Mapping(target = "district", source = "ward.district", qualifiedByName = "mapDistrictToResponse")
    @Mapping(target = "province", source = "ward.district.province", qualifiedByName = "mapProvinceToResponse")
    public abstract AddressDTO toDto(AddressBook entity);

    @Mapping(target = "ward", source = "wardCode", qualifiedByName = "mapWardCodeToEntity")
    public abstract AddressBook toEntity(AddressDTO dto);

    @Named("mapWardCodeToEntity")
    protected Ward mapWardCodeToEntity(String wardCode) {
        if (wardCode == null || wardCode.trim().isEmpty())
            return null;
        return wardRepository.findById(wardCode)
                .orElseThrow(() -> new AppException(ErrorCode.WARD_NOT_FOUND));
    }

    @Named("mapWardToResponse")
    protected WardResponse mapWardToResponse(Ward ward) {
        if (ward == null)
            return null;
        return new WardResponse(ward.getCode(), ward.getFullName());
    }

    @Named("mapDistrictToResponse")
    protected DistrictResponse mapDistrictToResponse(District district) {
        if (district == null)
            return null;
        return new DistrictResponse(district.getCode(), district.getFullName());
    }

    @Named("mapProvinceToResponse")
    protected ProvinceResponse mapProvinceToResponse(Province province) {
        if (province == null)
            return null;
        return new ProvinceResponse(province.getCode(), province.getFullName());
    }
}