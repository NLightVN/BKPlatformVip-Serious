package com.example.backend.mapper;

import com.example.backend.dto.request.AddressDTO;
import com.example.backend.entity.AddressBook;
import com.example.backend.entity.Ward;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(source = "ward.code", target = "wardCode")
    AddressDTO toDto(AddressBook entity);

    @Mapping(source = "wardCode", target = "ward", qualifiedByName = "stringToWard")
    AddressBook toEntity(AddressDTO dto);

    @Named("stringToWard")
    default Ward stringToWard(String wardCode) {
        if (wardCode == null || wardCode.isEmpty()) return null;
        return Ward.builder().code(wardCode).build();
    }
}