package com.outsera.awards.application.port.in;

import com.outsera.awards.domain.model.Movie;
import com.outsera.awards.domain.model.ProducerIntervals;
import java.util.List;

public interface ImportMoviesUseCase {

    ProducerIntervals importMovies(List<Movie> movies);
}
