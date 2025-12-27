package com.example.backend.mapper;

import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.request.ShopCreationRequest;
import com.example.backend.dto.response.ShopResponse;
import com.example.backend.entity.AddressBook;
import com.example.backend.entity.Shop;
import com.example.backend.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T15:55:48+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class ShopMapperImpl implements ShopMapper {

    @Override
    public ShopResponse toShopResponse(Shop shop) {
        if ( shop == null ) {
            return null;
        }

        ShopResponse.ShopResponseBuilder shopResponse = ShopResponse.builder();

        shopResponse.ownerId( shopOwnerUserId( shop ) );
        shopResponse.ownerUsername( shopOwnerUsername( shop ) );
        shopResponse.shopId( shop.getShopId() );
        shopResponse.name( shop.getName() );
        shopResponse.address( addressBookToAddressDTO( shop.getAddress() ) );

        return shopResponse.build();
    }

    @Override
    public Shop toShop(ShopCreationRequest shopCreationRequest) {
        if ( shopCreationRequest == null ) {
            return null;
        }

        Shop.ShopBuilder shop = Shop.builder();

        shop.name( shopCreationRequest.getName() );
        shop.address( addressDTOToAddressBook( shopCreationRequest.getAddress() ) );

        return shop.build();
    }

    private String shopOwnerUserId(Shop shop) {
        User owner = shop.getOwner();
        if ( owner == null ) {
            return null;
        }
        return owner.getUserId();
    }

    private String shopOwnerUsername(Shop shop) {
        User owner = shop.getOwner();
        if ( owner == null ) {
            return null;
        }
        return owner.getUsername();
    }

    protected AddressDTO addressBookToAddressDTO(AddressBook addressBook) {
        if ( addressBook == null ) {
            return null;
        }

        AddressDTO.AddressDTOBuilder addressDTO = AddressDTO.builder();

        addressDTO.phone( addressBook.getPhone() );
        addressDTO.addressDetail( addressBook.getAddressDetail() );
        addressDTO.ward( addressBook.getWard() );
        addressDTO.district( addressBook.getDistrict() );
        addressDTO.province( addressBook.getProvince() );
        addressDTO.googleMapId( addressBook.getGoogleMapId() );

        return addressDTO.build();
    }

    protected AddressBook addressDTOToAddressBook(AddressDTO addressDTO) {
        if ( addressDTO == null ) {
            return null;
        }

        AddressBook.AddressBookBuilder addressBook = AddressBook.builder();

        addressBook.phone( addressDTO.getPhone() );
        addressBook.addressDetail( addressDTO.getAddressDetail() );
        addressBook.ward( addressDTO.getWard() );
        addressBook.district( addressDTO.getDistrict() );
        addressBook.province( addressDTO.getProvince() );
        addressBook.googleMapId( addressDTO.getGoogleMapId() );

        return addressBook.build();
    }
}
