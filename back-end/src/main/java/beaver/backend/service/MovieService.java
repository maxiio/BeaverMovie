package beaver.backend.service;

import beaver.backend.entity.Cinema;
import beaver.backend.entity.Movie;
import beaver.backend.entity.Showtime;
import beaver.backend.entity.responseType.ResponseMovies;
import beaver.backend.repository.CinemaRepository;
import beaver.backend.repository.MovieRepository;
import beaver.backend.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by parda on 2017/5/27.
 */
@Service
public class MovieService {
    @Autowired
    MovieRepository movieRepository;

    @Autowired
    ShowtimeRepository showtimeRepository;

    @Autowired
    CinemaRepository cinemaRepository;

    @Autowired
    RestTemplate restTemplate;

    public List<Movie> addMovies() {
        ResponseEntity<ResponseMovies> responseEntity = restTemplate.exchange("http://localhost:3000/", HttpMethod.GET,
                null, new ParameterizedTypeReference<ResponseMovies>() { });
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody().getState().equals("success")) {
            List<Movie> movies = responseEntity.getBody().getMovies();
            movies = movies.stream()
                    .filter(movie -> movieRepository.findOne(movie.getId()) == null)
                    .collect(Collectors.toList());
            movieRepository.save(movies);
            addMovieShowtimes(movies);

            if (movies.size() < 8) {
                List<Movie> old_movies = movieRepository.findAll()
                        .stream()
                        .skip((int)(Math.random()*5))
                        .collect(Collectors.toList());
                addMovieShowtimes(old_movies);
                movies.addAll(old_movies);
            }
            return movies;
        }
        return null;
    }

    private void addMovieShowtimes(List<Movie> movies) {
        List<Cinema> cinemas = cinemaRepository.findAll();
        movies.forEach(movie -> {
            int cost = (int)(Math.random()*15) + 30;
            cinemas.forEach(cinema -> {
                for (int i = 0; i < 10; i++) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, (int)(Math.random()*8));
                    calendar.add(Calendar.HOUR_OF_DAY, (int)(Math.random()*5 + 1));
                    showtimeRepository.save(new Showtime(cinema, calendar.getTime(), movie, cost));
                }
            });
        });
    }

    public ResponseEntity getMovieDetail(long id) {
        return restTemplate.exchange("http://localhost:3000/movieItem/{id}", HttpMethod.GET,
                null, Map.class, id);
    }

    public Optional<Movie> getOne(long id) {
        return Optional.of(movieRepository.findOne(id));
    }

    public List<Movie> getLastest(final int startNum) {
        List<Movie> list = new ArrayList<>();
        movieRepository.findAll()
                .forEach(movie -> {
                    if (!checkShowtimeEmpty(movie))
                        list.add(movie);
                });
        return list.stream()
                .skip(startNum)
                .limit(10)
                .collect(Collectors.toList());
    }

    public Set<Showtime> getShowtimes(long id) {
        return movieRepository.findOne(id).getShowtimes()
                .stream()
                .filter(showtime -> showtime.getStartTime().after(Calendar.getInstance().getTime()))
                .collect(Collectors.toSet());
    }

    private boolean checkShowtimeEmpty(Movie movie) {
        return movie.getShowtimes()
                .stream()
                .filter(showtime -> showtime.getStartTime().after(Calendar.getInstance().getTime()))
                .count() == 0;
    }
}
