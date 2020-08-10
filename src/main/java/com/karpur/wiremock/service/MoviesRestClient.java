package com.karpur.wiremock.service;

import com.karpur.wiremock.dto.Movie;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.karpur.wiremock.constants.MoviesAppConstants.GET_ALL_MOVIES_V1;

public class MoviesRestClient {

    private WebClient webClient;

    public MoviesRestClient(WebClient webClient){
        this.webClient = webClient;
    }

    public List<Movie> retrieveAllMovies(){

        return webClient.get().uri(GET_ALL_MOVIES_V1)
                        .retrieve()
                        .bodyToFlux(Movie.class)
                        .collectList()
                        .block();

    }
}
