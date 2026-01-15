package com.example.backend.mapper;

import com.example.backend.dto.request.AddressDTO;
import com.example.backend.entity.AddressBook;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T15:55:47+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public AddressDTO toDto(AddressBook entity) {
        if ( entity == null ) {
            return null;
        }

        AddressDTO.AddressDTOBuilder addressDTO = AddressDTO.builder();

        addressDTO.phone( entity.getPhone() );
        addressDTO.addressDetail( entity.getAddressDetail() );
        addressDTO.ward( entity.getWard() );
        addressDTO.district( entity.getDistrict() );
        addressDTO.province( entity.getProvince() );
        addressDTO.googleMapId( entity.getGoogleMapId() );

        return addressDTO.build();
    }

    @Override
    public AddressBook toEntity(AddressDTO dto) {
        if ( dto == null ) {
            return null;
        }

        AddressBook.AddressBookBuilder addressBook = AddressBook.builder();

        addressBook.phone( dto.getPhone() );
        addressBook.addressDetail( dto.getAddressDetail() );
        addressBook.ward( dto.getWard() );
        addressBook.district( dto.getDistrict() );
        addressBook.province( dto.getProvince() );
        addressBook.googleMapId( dto.getGoogleMapId() );

        return addressBook.build();
    }
}
