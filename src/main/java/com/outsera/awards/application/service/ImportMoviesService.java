package com.outsera.awards.application.service;

import com.outsera.awards.application.port.in.CalculateProducerIntervalsUseCase;
import com.outsera.awards.application.port.in.ImportMoviesUseCase;
import com.outsera.awards.application.port.out.MovieCatalogPort;
import com.outsera.awards.domain.model.Movie;
import com.outsera.awards.domain.model.ProducerIntervals;
import java.util.List;

public class ImportMoviesService implements ImportMoviesUseCase {

    private final MovieCatalogPort movieCatalog;
    private final CalculateProducerIntervalsUseCase calculateProducerIntervals;

    public ImportMoviesService(
            MovieCatalogPort movieCatalog,
            CalculateProducerIntervalsUseCase calculateProducerIntervals) {
        this.movieCatalog = movieCatalog;
        this.calculateProducerIntervals = calculateProducerIntervals;
    }

    @Override
    public ProducerIntervals importMovies(List<Movie> movies) {
        movieCatalog.replaceAll(movies);
        return calculateProducerIntervals.calculate();
    }
}
