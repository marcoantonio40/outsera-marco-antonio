package com.outsera.awards.application.service;

import com.outsera.awards.application.port.in.CalculateProducerIntervalsUseCase;
import com.outsera.awards.application.port.out.MovieCatalogPort;
import com.outsera.awards.application.dto.MovieDTO;
import com.outsera.awards.application.dto.ProducerIntervalDTO;
import com.outsera.awards.application.dto.ProducerIntervalsDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CalculateProducerIntervalsService implements CalculateProducerIntervalsUseCase {

    private static final Pattern PRODUCER_SEPARATOR =
            Pattern.compile("\\s*,\\s*(?:and\\s+)?|\\s+and\\s+");

    private final MovieCatalogPort movieCatalog;

    @Override
    public ProducerIntervalsDTO calculate() {
        List<MovieDTO> winningMovies = movieCatalog.findWinningMovies();
        String fileName = winningMovies.isEmpty() ? "desconhecido" : winningMovies.get(0).fileName();
        log.info("CPIS-CAL-00 | Iniciando cálculo dos intervalos | fileName={} winningMovieCount={}",
                fileName, winningMovies.size());
        Map<String, Integer> lastWinByProducer = new HashMap<>();
        List<ProducerIntervalDTO> minIntervals = new ArrayList<>();
        List<ProducerIntervalDTO> maxIntervals = new ArrayList<>();
        int minInterval = Integer.MAX_VALUE;
        int maxInterval = Integer.MIN_VALUE;
        int intervalCount = 0;

        for (MovieDTO movie : winningMovies) {
            for (String producer : PRODUCER_SEPARATOR.split(movie.producers())) {
                String normalizedProducer = producer.trim();
                if (normalizedProducer.isEmpty()) {
                    continue;
                }

                Integer previousWin = lastWinByProducer.put(normalizedProducer, movie.year());
                if (previousWin == null) {
                    continue;
                }

                int interval = movie.year() - previousWin;
                ProducerIntervalDTO result = new ProducerIntervalDTO(
                        normalizedProducer, interval, previousWin, movie.year());
                intervalCount++;

                if (interval < minInterval) {
                    minInterval = interval;
                    minIntervals.clear();
                }
                if (interval == minInterval) {
                    minIntervals.add(result);
                }

                if (interval > maxInterval) {
                    maxInterval = interval;
                    maxIntervals.clear();
                }
                if (interval == maxInterval) {
                    maxIntervals.add(result);
                }
            }
        }

        if (intervalCount == 0) {
            log.info("CPIS-CAL-01 | Nenhum intervalo consecutivo encontrado | fileName={}", fileName);
            return new ProducerIntervalsDTO(List.of(), List.of());
        }

        Comparator<ProducerIntervalDTO> resultOrder = Comparator
                .comparing(ProducerIntervalDTO::producer)
                .thenComparingInt(ProducerIntervalDTO::previousWin)
                .thenComparingInt(ProducerIntervalDTO::followingWin);
        minIntervals.sort(resultOrder);
        maxIntervals.sort(resultOrder);

        log.info("CPIS-CAL-02 | Intervalos calculados | fileName={} producerCount={} intervalCount={} min={} max={}",
                fileName, lastWinByProducer.size(), intervalCount, minInterval, maxInterval);
        return new ProducerIntervalsDTO(minIntervals, maxIntervals);
    }
}
