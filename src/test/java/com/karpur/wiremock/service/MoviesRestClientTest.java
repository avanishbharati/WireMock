package com.karpur.wiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.karpur.wiremock.dto.Movie;
import com.karpur.wiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;


import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.karpur.wiremock.constants.MoviesAppConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WireMockExtension.class)
public class MoviesRestClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoviesRestClientTest.class);

    @InjectServer
    WireMockServer wireMockServer;

    @ConfigureWireMock
    Options options = wireMockConfig()
        .port(8088)
        .notifier(new ConsoleNotifier(true))
        .extensions(new ResponseTemplateTransformer(true));

    MoviesRestClient moviesRestClient;
    WebClient webClient;


    @BeforeEach
    void setUp(){
        int port = wireMockServer.port();
        String baseUrl = String.format("http://localhost:%s", port);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);

        stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom("http://localhost:8081")));

    }

    @Test
    void retrieveAllMovies(){

        //given

        stubFor(get(anyUrl())
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("all-movies.json")));

        //When
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        LOGGER.info("Number of movies: {}", movieList.size());
        LOGGER.info("Movie List : {}", movieList);

        //Then
        assertTrue(movieList.size() > 0);

    }

    @Test
    void retrieveAllMovies_matchesUrl(){

        //given

        stubFor(get(urlPathEqualTo(GET_ALL_MOVIES_V1))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("all-movies.json")));

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

        stubFor(get(urlPathEqualTo("/movieservice/v1/movie/1"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("movie.json")));

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        LOGGER.info("Movie : {}", movie.getName());
        //then
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void retrieveMovieById_regEx() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("movie.json")));

        Integer movieId = 9;

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        LOGGER.info("Movie : {}", movie.getName());
        //then
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void retrieveMovieById_responseTemplating() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("movie-template.json")));

        Integer movieId = 8;

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        LOGGER.info("Movie : {}", movie);
        //then
        assertEquals("Batman Begins", movie.getName());
        assertEquals(8, movie.getMovie_id().intValue());
    }


    @Test
    void retrieveMovieById_NotFound() {
        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("404-movieId.json")));
        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(movieId));

    }

    //query param
    @Test
    void retrieveMovieByName() {
        //given
        String movieName = "Avengers";

        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("avengers.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }

    //query param
    @Test
    void retrieveMovieByName_approach2() {
        //given
        String movieName = "Avengers";

        stubFor(get(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
            .withQueryParam("movie_name", equalTo(movieName))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("avengers.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }

    @Test
    void retrieveMovieByName_responseTemplating() {
        //given
        String movieName = "Avengers";

        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("movie-byName-template.json")));

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
        List<Movie> movieList = moviesRestClient.retrieveMovieByYear(year);

        //then
        assertEquals(2, movieList.size());

    }

    @Test
    void retrieveMovieByYear_withStub() {
        //given
        Integer year = 2012;

        stubFor(get(urlPathEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1))
            .withQueryParam("year", equalTo(year.toString()))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("year-template.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByYear(year);

        //then
        assertEquals(2, movieList.size());

    }

    @Test
    void retrieveMovieByYear_bySpyros() {
        //given
        Integer year = 2012;

        //when
        // List<Movie> movieList = moviesRestClient.retrieveMovieByYear(year);
        stubFor(get((urlEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1 + "?year="+year)))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("year-template.json"))) ;


        List<Movie> movieList = moviesRestClient.retrieveMovieByYear(year);
        //then
        assertEquals(2, movieList.size());

    }


    @Test
    void retrieveMovieByYear_NotFound() {
        //given
        Integer year = 1980;
        stubFor(get(urlPathEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1))
            .withQueryParam("year", equalTo(year.toString()))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("404-movieyear.json")));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByYear(year));

    }

    @Test
    void retrieveMovieByYear_Not_Found() {
        //given
        Integer year = 1950;
        stubFor(get(urlEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1+"?year="+year))
            .withQueryParam("year", equalTo(year.toString()))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("404-movieyear.json")));
        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByYear(year));
    }

    @Test
    void retrieveMovieByYear_not_found_approach2() {
        //given
        Integer movieYear = 1950;

        stubFor(get(urlEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1 + "?year=" + movieYear))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("404-movieYear.json")));
        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByYear(movieYear));
    }


    @Test
    void addNewMovie() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));

        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath(("$.name"), equalTo("Toy Story 4")))
            .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom")))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("add-movie.json")));

        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        //then
        assertTrue(movie.getMovie_id() != null);

    }

    @Test
    void addNewMovie_responseTemplating() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));

        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath(("$.name"), equalTo("Toy Story 4")))
            .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom")))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("add-movie-template.json")));

        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        LOGGER.info("Response {}", movie);
        //then
        assertTrue(movie.getMovie_id() != null);

    }

    @Test
    void addMovie_badRequest() {
        //given
        Movie movie = new Movie(null, null, 2019, "Tom Hanks, Tim Allen", LocalDate.of(2019, 06, 20));
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom")))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("400-invalid-input.json")));

        //when
        String expectedErrorMessage = "Please pass all the input fields : [name]";
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addNewMovie(movie), expectedErrorMessage);
    }

    @Test
    @DisplayName("Passing the Movie name and year as Null")
    void addNewMovie_InvalidInput() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, null, null, batmanBeginsCrew, LocalDate.of(2019, 06, 20));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addNewMovie(toyStory));

    }

    @Test
    void updateMovie() {
        //given
        String darkNightRisesCrew = "ABC";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 3;

        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
            .withRequestBody(matchingJsonPath(("$.cast"), containing(darkNightRisesCrew)))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("updatemovie-template.json")));

        //when
        Movie updatedMovie = moviesRestClient.updateMovie(movieId, darkNightRises);

        //then
        assertTrue(updatedMovie.getCast().contains(darkNightRisesCrew));


    }

    @Test
    void updateMovie_Not_Found() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 100;

        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
            .withRequestBody(matchingJsonPath(("$.cast"), containing(darkNightRisesCrew)))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));


        //when
        Assertions.assertThrows(MovieErrorResponse.class,()-> moviesRestClient.updateMovie(movieId, darkNightRises));
    }

    @Test
    void deleteMovie() {

        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 5", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));

        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath(("$.name"), equalTo("Toy Story 5")))
            .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom")))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("add-movie-template.json")));

        Movie movie = moviesRestClient.addNewMovie(toyStory);
        String expectedResponse = "Movie Deleted Successfully";

        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(expectedResponse)));

        Integer movieId=movie.getMovie_id().intValue();

        //when
        String response = moviesRestClient.deleteMovieById(movieId);

        //then

        assertEquals(expectedResponse, response);

    }

    @Test
    void deleteMovie_notFound() {

        //given
        Integer movieId=100;
        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.deleteMovieById(movieId)) ;

    }

    //Verify stubs
    @Test
    void deleteMovieByName() {
        //given
        Movie movie = new Movie(null, "Toys Story 5", 2019,"Tom Hanks, Tim Allen", LocalDate.of(2019, 06, 20));

        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath(("$.name"),equalTo("Toys Story 5")))
            .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom")))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("add-movie-template.json")));
        Movie addedMovie = moviesRestClient.addNewMovie(movie);

        String expectedErrorMessage = "Movie Deleted Successfully";
        stubFor(delete(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=Toys%20Story%205"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        //when
        String responseMessage = moviesRestClient.deleteMovieByName(addedMovie.getName());

        //then
        assertEquals(expectedErrorMessage, responseMessage);

        //Verify DSL ( see deleteMovieByName )
        verify(exactly(1),postRequestedFor(urlPathEqualTo(ADD_MOVIE_V1))
            .withRequestBody(matchingJsonPath(("$.name"),equalTo("Toys Story 5")))
            .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom"))));

        //check if the delete call was made
        verify(exactly(1),deleteRequestedFor(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1+"?movie_name=Toys%20Story%205")));

    }

    @Test
    @Disabled
    void getAllMovies_Exception() {
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());
    }

    @Test
    void deleteMovieByName_selectiveproxying() {
        //given
        Movie movie = new Movie(null, "Toys Story 5",  2019, "Tom Hanks, Tim Allen", LocalDate.of(2019, 06, 20));
        Movie addedMovie = moviesRestClient.addNewMovie(movie);

        String expectedErrorMessage = "Movie Deleted Successfully";
        stubFor(delete(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1+"?movie_name=Toys%20Story%205"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        //when
        String responseMessage = moviesRestClient.deleteMovieByName(addedMovie.getName());

        //then
        assertEquals(expectedErrorMessage, responseMessage);

        verify(exactly(1),deleteRequestedFor(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1+"?movie_name=Toys%20Story%205")));

    }

}
