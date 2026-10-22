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

public class CalculateProducerIntervalsService implements CalculateProducerIntervalsUseCase {

    private static final Pattern PRODUCER_SEPARATOR =
            Pattern.compile("\\s*,\\s*(?:and\\s+)?|\\s+and\\s+");

    private final MovieCatalogPort movieCatalog;

    public CalculateProducerIntervalsService(MovieCatalogPort movieCatalog) {
        this.movieCatalog = movieCatalog;
    }

    @Override
    public ProducerIntervals calculate() {
        Map<String, List<Integer>> winsByProducer = new TreeMap<>();
        for (Movie movie : movieCatalog.findWinningMovies()) {
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

        return new ProducerIntervals(
                intervals.stream()
                        .filter(interval -> interval.interval() == minInterval)
                        .toList(),
                intervals.stream()
                        .filter(interval -> interval.interval() == maxInterval)
                        .toList());
    }
}
