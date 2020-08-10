package com.karpur.wiremock.service;

import com.karpur.wiremock.dto.Movie;
import com.karpur.wiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void retrieveMovieById() {
        //given
        Integer movieId = 1;

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        LOGGER.info("Movie : {}", movie.getName());
        //then
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void retrieveMovieById_NotFound() {
        //given
        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(movieId));

    }

    @Test
    void retrieveMovieByName() {
        //given
        String movieName = "Avengers";

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }

    @Test
    void retrieveMovieByName_Not_Found() {
        //given
        String movieName = "ABC";

        //whenretrieveMovieByYear
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByName(movieName));
    }


    @Test
    void retrieveMovieByYear() {
        //given
        Integer year = 2012;

        //when
        List<Movie> movieList = moviesRestClient.retreieveMovieByYear(year);

        //then
        assertEquals(2, movieList.size());

    }

    @Test
    void retrieveMovieByYear_Not_Found() {
        //given
        Integer year = 1950;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retreieveMovieByYear(year));

    }

    @Test
    void addNewMovie() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));

        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        //then
        assertTrue(movie.getMovie_id() != null);

    }

    @Test
    @DisplayName("Passing the Movie name and year as Null")
    void addNewMovie_InvlaidInput() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, null, null, batmanBeginsCrew, LocalDate.of(2019, 06, 20));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addNewMovie(toyStory));

    }

    @Test
    void updateMovie() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 3;

        //when
        Movie updatedMovie = moviesRestClient.updateMovie(movieId, darkNightRises);

        //then
        String updatedCastName = "Christian Bale, Heath Ledger , Michael Caine, Tom Hardy";
        assertTrue(updatedMovie.getCast().contains(darkNightRisesCrew));


    }

    @Test
    void updateMovie_Not_Found() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class,()-> moviesRestClient.updateMovie(movieId, darkNightRises));
    }

    @Test
    void deleteMovie() {

        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));
        Movie movie = moviesRestClient.addNewMovie(toyStory);
        Integer movieId=movie.getMovie_id().intValue();

        //when
        String response = moviesRestClient.deleteMovieById(movieId);

        //then
        String expectedResponse = "Movie Deleted Successfully";
        assertEquals(expectedResponse, response);

    }

    @Test
    void deleteMovie_notFound() {

        //given
        Integer movieId=100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.deleteMovieById(movieId)) ;

    }


    @Test
    @Disabled
    void getAllMovies_Exception() {
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());
    }

}
