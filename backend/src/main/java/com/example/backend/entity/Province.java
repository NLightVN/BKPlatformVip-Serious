package com.example.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Entity @Table(name = "provinces")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Province {
    @Id String code;
    String fullName;

    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL)
    List<District> districts;
}