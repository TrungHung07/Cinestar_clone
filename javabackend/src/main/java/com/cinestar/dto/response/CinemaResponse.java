package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaResponse {

    private Long id;
    private String name;
    private String slug;
    private String address;
    private String city;
    private String hotline;
    private String imageUrl;
    private Integer roomCount;
}
