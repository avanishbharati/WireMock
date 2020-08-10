package com.karpur.wiremock.service;

import com.karpur.wiremock.dto.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesRestClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoviesRestClientTest.class);


    MoviesRestClient moviesRestClient;
    WebClient webClient;


    @BeforeEach
    void setUp(){

        String baseUrl = "http://localhost:8081";
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);

    }

    @Test
    void retrieveAllMovies(){

        //When
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        LOGGER.info("Number of movies: {}", movieList.size());
        LOGGER.info("Movie List : {}", movieList);

        //Then
        assertTrue(movieList.size() > 0);

    }


}
