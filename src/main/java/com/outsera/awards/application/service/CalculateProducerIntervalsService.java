package com.outsera.awards.application.service;

import com.outsera.awards.application.port.in.CalculateProducerIntervalsUseCase;
import com.outsera.awards.application.port.out.MovieCatalogPort;
import com.outsera.awards.domain.dto.MovieDTO;
import com.outsera.awards.domain.dto.ProducerIntervalDTO;
import com.outsera.awards.domain.dto.ProducerIntervalsDTO;
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
    public ProducerIntervalsDTO calculate() {
        List<MovieDTO> winningMovies = movieCatalog.findWinningMovies();
        String fileName = winningMovies.isEmpty() ? "desconhecido" : winningMovies.get(0).fileName();
        log.info("CPIS-CAL-00 | Iniciando cálculo dos intervalos | fileName={} winningMovieCount={}",
                fileName, winningMovies.size());
        Map<String, List<Integer>> winsByProducer = new TreeMap<>();
        for (MovieDTO movie : winningMovies) {
            for (String producer : PRODUCER_SEPARATOR.split(movie.producers())) {
                String normalizedProducer = producer.trim();
                if (!normalizedProducer.isEmpty()) {
                    winsByProducer.computeIfAbsent(normalizedProducer, key -> new ArrayList<>())
                            .add(movie.year());
                }
            }
        }

        List<ProducerIntervalDTO> intervals = new ArrayList<>();
        winsByProducer.forEach((producer, years) -> {
            years.sort(Comparator.naturalOrder());
            for (int index = 1; index < years.size(); index++) {
                int previousWin = years.get(index - 1);
                int followingWin = years.get(index);
                intervals.add(new ProducerIntervalDTO(
                        producer, followingWin - previousWin, previousWin, followingWin));
            }
        });

        if (intervals.isEmpty()) {
            log.info("CPIS-CAL-01 | Nenhum intervalo consecutivo encontrado | fileName={}", fileName);
            return new ProducerIntervalsDTO(List.of(), List.of());
        }

        int minInterval = intervals.stream()
                .mapToInt(ProducerIntervalDTO::interval)
                .min()
                .orElseThrow();
        int maxInterval = intervals.stream()
                .mapToInt(ProducerIntervalDTO::interval)
                .max()
                .orElseThrow();

        ProducerIntervalsDTO result = new ProducerIntervalsDTO(
                intervals.stream()
                        .filter(interval -> interval.interval() == minInterval)
                        .toList(),
                intervals.stream()
                        .filter(interval -> interval.interval() == maxInterval)
                        .toList());
        log.info("CPIS-CAL-02 | Intervalos calculados | fileName={} producerCount={} intervalCount={} min={} max={}",
                fileName, winsByProducer.size(), intervals.size(), minInterval, maxInterval);
        return result;
    }
}
