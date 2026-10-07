package com.cinestar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    /**
     * Luu duoi dang "ROLE_ADMIN", "ROLE_USER" de khop voi Spring Security hasRole()
     */
    @Column(nullable = false, unique = true, length = 30)
    private String name;

    private String description;
}
