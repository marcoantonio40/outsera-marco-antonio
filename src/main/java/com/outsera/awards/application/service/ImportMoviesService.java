package com.outsera.awards.application.service;

import com.outsera.awards.application.port.in.CalculateProducerIntervalsUseCase;
import com.outsera.awards.application.port.in.ImportMoviesUseCase;
import com.outsera.awards.application.port.out.MovieCatalogPort;
import com.outsera.awards.application.dto.MovieDTO;
import com.outsera.awards.application.dto.ProducerIntervalsDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    public ProducerIntervalsDTO importMovies(List<MovieDTO> movies) {
        String fileName = movies.isEmpty() ? "desconhecido" : movies.get(0).fileName();

        log.info("IMS-IM-00 | Iniciando importação de filmes | fileName={} movieCount={}",
                fileName, movies.size());

        movieCatalog.replaceAll(movies);

        log.info("IMS-IM-01 | Filmes importados | fileName={} movieCount={}",
                fileName, movies.size());

        ProducerIntervalsDTO intervals = calculateProducerIntervals.calculate();

        log.info("IMS-IM-02 | Cálculo concluído | fileName={} minCount={} maxCount={}",
                fileName, intervals.min().size(), intervals.max().size());

        return intervals;
    }
}
