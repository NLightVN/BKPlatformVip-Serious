package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressBook {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id")
    String addressId;

    String name;
    String phone;
    String addressDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_code")
    Ward ward;

    public District getDistrict() {
        return ward != null ? ward.getDistrict() : null;
    }


    public Province getProvince() {
        return (ward != null && ward.getDistrict() != null)
                ? ward.getDistrict().getProvince()
                : null;
    }
}
