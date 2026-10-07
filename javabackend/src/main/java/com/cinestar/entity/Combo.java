package com.cinestar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Bap nuoc, combo di kem */
@Entity
@Table(name = "combos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Combo extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    private Boolean active = true;
}
