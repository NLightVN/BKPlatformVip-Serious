package com.example.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Entity @Table(name = "districts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class District {
    @Id String code; // ID từ GHN
    String fullName;

    @ManyToOne @JoinColumn(name = "province_code")
    Province province;

    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL)
    List<Ward> wards;
}