package com.karpur.wiremock.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    private Long movie_id;
    private String name;
    private Integer year;
    private String cast;
    private LocalDate release_date;
}