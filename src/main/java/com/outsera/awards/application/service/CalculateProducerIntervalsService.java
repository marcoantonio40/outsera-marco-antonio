package com.outsera.awards.application.service;

import com.outsera.awards.application.port.in.CalculateProducerIntervalsUseCase;
import com.outsera.awards.application.port.out.MovieCatalogPort;
import com.outsera.awards.domain.model.Movie;
import com.outsera.awards.domain.model.ProducerInterval;
import com.outsera.awards.domain.model.ProducerIntervals;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CalculateProducerIntervalsService implements CalculateProducerIntervalsUseCase {

    private static final Pattern PRODUCER_SEPARATOR =
            Pattern.compile("\\s*,\\s*(?:and\\s+)?|\\s+and\\s+");

    private final MovieCatalogPort movieCatalog;

    public CalculateProducerIntervalsService(MovieCatalogPort movieCatalog) {
        this.movieCatalog = movieCatalog;
    }

    @Override
    public ProducerIntervals calculate() {
        List<Movie> winningMovies = movieCatalog.findWinningMovies();
        String fileName = winningMovies.isEmpty() ? "desconhecido" : winningMovies.get(0).fileName();
        log.info("UPC-CA-01 | Iniciando cálculo dos intervalos | fileName={} winningMovieCount={}",
                fileName, winningMovies.size());
        Map<String, List<Integer>> winsByProducer = new TreeMap<>();
        for (Movie movie : winningMovies) {
            for (String producer : PRODUCER_SEPARATOR.split(movie.producers())) {
                String normalizedProducer = producer.trim();
                if (!normalizedProducer.isEmpty()) {
                    winsByProducer.computeIfAbsent(normalizedProducer, key -> new ArrayList<>())
                            .add(movie.year());
                }
            }
        }

        List<ProducerInterval> intervals = new ArrayList<>();
        winsByProducer.forEach((producer, years) -> {
            years.sort(Comparator.naturalOrder());
            for (int index = 1; index < years.size(); index++) {
                int previousWin = years.get(index - 1);
                int followingWin = years.get(index);
                intervals.add(new ProducerInterval(
                        producer, followingWin - previousWin, previousWin, followingWin));
            }
        });

        if (intervals.isEmpty()) {
            log.info("UPC-CA-03 | Nenhum intervalo consecutivo encontrado | fileName={}", fileName);
            return new ProducerIntervals(List.of(), List.of());
        }

        int minInterval = intervals.stream()
                .mapToInt(ProducerInterval::interval)
                .min()
                .orElseThrow();
        int maxInterval = intervals.stream()
                .mapToInt(ProducerInterval::interval)
                .max()
                .orElseThrow();

        ProducerIntervals result = new ProducerIntervals(
                intervals.stream()
                        .filter(interval -> interval.interval() == minInterval)
                        .toList(),
                intervals.stream()
                        .filter(interval -> interval.interval() == maxInterval)
                        .toList());
        log.info("UPC-CA-04 | Intervalos calculados | fileName={} producerCount={} intervalCount={} min={} max={}",
                fileName, winsByProducer.size(), intervals.size(), minInterval, maxInterval);
        return result;
    }
}
