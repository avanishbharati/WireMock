package com.karpur.wiremock.service;

import com.karpur.wiremock.dto.Movie;
import com.karpur.wiremock.exception.MovieErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static com.karpur.wiremock.constants.MoviesAppConstants.*;

public class MoviesRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoviesRestClient.class);

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

    public Movie retrieveMovieById(Integer movieId) {
        String movieByIdURL =  MOVIE_BY_ID_PATH_PARAM_V1;
        Movie movie;
        try {
            movie = webClient.get().uri(movieByIdURL, movieId) //mapping the movie id to the url
                .retrieve()
                .bodyToMono(Movie.class) //body is converted to Mono(Represents single item)
                .block();
        } catch (WebClientResponseException ex) {
            LOGGER.error("WebClientResponseException - Error Message is : {} ", ex, ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            LOGGER.error("Exception - The Error Message is {} ", ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
        return movie;
    }

    public List<Movie> retrieveMovieByName(String movieName) {

        List<Movie> movieList = null;
        String retrieveByNameUri = UriComponentsBuilder.fromUriString( MOVIE_BY_NAME_QUERY_PARAM_V1)
            .queryParam("movie_name", movieName)
            .buildAndExpand()
            .toUriString();

        try {
            movieList = webClient.get().uri(retrieveByNameUri)
                .retrieve()
                .bodyToFlux(Movie.class)
                .collectList()
                .block();
        } catch (WebClientResponseException ex) {
            LOGGER.error("WebClientResponseException in retrieveMovieByName - Error Message is : {} ", ex, ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            LOGGER.error("Exception - The Error Message is {} ", ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
        return movieList;
    }


    /**
     * This method makes a REST call to the Movies RESTFUL Service and retrieves a list of Movies as a response based on the year.
     *
     * @param year - Integer (Example : 2012,2013 etc.,)
     * @return - List<Movie>
     */
    public List<Movie> retreieveMovieByYear(Integer year) {
        String retrieveByYearUri = UriComponentsBuilder.fromUriString( MOVIE_BY_YEAR_QUERY_PARAM_V1)
            .queryParam("year", year)
            .buildAndExpand()
            .toUriString();
        List<Movie> movieList;

        try {
            movieList = webClient.get().uri(retrieveByYearUri)
                .retrieve()
                .bodyToFlux(Movie.class)
                .collectList()
                .block();
        } catch (WebClientResponseException ex) {
            LOGGER.error("WebClientResponseException in retreieveMovieByYear - Error Message is : {} ", ex, ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            LOGGER.error("Exception - The Error Message is {} ", ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
        return movieList;
    }

    /**
     * This method makes the REST call to add a new Movie to the Movies RESTFUL Service.
     *
     * @param newMovie
     * @return
     */
    public Movie addNewMovie(Movie newMovie) {
        Movie movie;

        try {
            movie = webClient.post().uri( ADD_MOVIE_V1)
                .syncBody(newMovie)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();
            LOGGER.info("New Movie SuccessFully addded {} ", movie);
        } catch (WebClientResponseException ex) {
            LOGGER.error("WebClientResponseException - Error Message is : {} , and the Error Response Body is {}", ex, ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            LOGGER.error("Exception - The Error Message is {} ", ex.getMessage());
            throw new MovieErrorResponse(ex);
        }
        return movie;
    }

    public Movie updateMovie(Integer movieId, Movie movie) {
        Movie updatedMovie;

        try {
            updatedMovie = webClient.put().uri( MOVIE_BY_ID_PATH_PARAM_V1, movieId)
                .syncBody(movie)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();
            LOGGER.info(" Movie SuccessFully updated {} ", updatedMovie);
        } catch (WebClientResponseException ex) {
            LOGGER.error("WebClientResponseException - Error Message is : {}", ex, ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            LOGGER.error("Exception - The Error Message is {} ", ex.getMessage());
            throw new MovieErrorResponse(ex);
        }

        return updatedMovie;
    }

    public String deleteMovieById(Integer movieId) {

        String response;
        try {
            response = webClient.delete().uri( MOVIE_BY_ID_PATH_PARAM_V1, movieId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        }catch (WebClientResponseException ex) {
            LOGGER.error("WebClientResponseException - Error Message is : {}", ex, ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            LOGGER.error("Exception - The Error Message is {} ", ex.getMessage());
            throw new MovieErrorResponse(ex);
        }

        return response;

    }
}
