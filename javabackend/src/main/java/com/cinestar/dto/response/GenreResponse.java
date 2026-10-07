package com.cinestar.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenreResponse {

    private Long id;
    private String name;
    private String slug;

    public GenreResponse() {
    }

    public GenreResponse(Long id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }
}
