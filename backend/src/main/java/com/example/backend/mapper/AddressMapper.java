package com.example.backend.mapper;

import com.example.backend.dto.request.AddressDTO;
import com.example.backend.entity.AddressBook;
import com.example.backend.entity.Ward;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.WardRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class AddressMapper {

    @Autowired
    protected WardRepository wardRepository;

    @Mapping(source = "name", target = "name")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "addressDetail", target = "addressDetail")
    @Mapping(source = "ward.code", target = "wardCode")
    // Entity -> DTO
    public abstract AddressDTO toDto(AddressBook entity);


    // DTO -> Entity
    @Mapping(source = "wardCode", target = "ward")
    public abstract AddressBook toEntity(AddressDTO dto);

    // MapStruct sẽ tự gọi method này khi map String -> Ward
    protected Ward map(String wardCode) {
        if (wardCode == null || wardCode.isBlank()) {
            return null;
        }
        System.out.println("Ward code: " + wardCode);
        return wardRepository.findById(wardCode)
                .orElseThrow(() -> new AppException(ErrorCode.WARD_NOT_FOUND));
    }
}
