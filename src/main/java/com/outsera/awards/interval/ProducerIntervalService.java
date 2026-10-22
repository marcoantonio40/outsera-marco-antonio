package com.outsera.awards.interval;

import com.outsera.awards.movie.Movie;
import com.outsera.awards.movie.MovieRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ProducerIntervalService {

    private static final Pattern PRODUCER_SEPARATOR =
            Pattern.compile("\\s*,\\s*(?:and\\s+)?|\\s+and\\s+");

    private final MovieRepository movieRepository;

    public ProducerIntervalService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public ProducerIntervalsResponse calculate() {
        Map<String, List<Integer>> winsByProducer = new TreeMap<>();
        for (Movie movie : movieRepository.findByWinnerTrueOrderByYearAsc()) {
            for (String producer : PRODUCER_SEPARATOR.split(movie.getProducers())) {
                String normalizedProducer = producer.trim();
                if (!normalizedProducer.isEmpty()) {
                    winsByProducer.computeIfAbsent(normalizedProducer, key -> new ArrayList<>())
                            .add(movie.getYear());
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
            return new ProducerIntervalsResponse(List.of(), List.of());
        }

        int minInterval = intervals.stream()
                .mapToInt(ProducerInterval::interval)
                .min()
                .orElseThrow();
        int maxInterval = intervals.stream()
                .mapToInt(ProducerInterval::interval)
                .max()
                .orElseThrow();

        return new ProducerIntervalsResponse(
                intervals.stream()
                        .filter(interval -> interval.interval() == minInterval)
                        .toList(),
                intervals.stream()
                        .filter(interval -> interval.interval() == maxInterval)
                        .toList());
    }
}
