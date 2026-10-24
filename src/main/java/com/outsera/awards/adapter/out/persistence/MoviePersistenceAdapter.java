package com.outsera.awards.adapter.out.persistence;

import com.outsera.awards.application.port.out.MovieCatalogPort;
import com.outsera.awards.adapter.out.persistence.entity.MovieEntity;
import com.outsera.awards.domain.dto.MovieDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Slf4j
public class MoviePersistenceAdapter implements MovieCatalogPort {

    private final MovieJpaRepository movieJpaRepository;

    public MoviePersistenceAdapter(MovieJpaRepository movieJpaRepository) {
        this.movieJpaRepository = movieJpaRepository;
    }

    @Override
    public List<MovieDTO> findWinningMovies() {
        List<MovieDTO> winningMovies = movieJpaRepository.findByWinnerTrueOrderByYearAsc().stream()
                .map(MovieEntity::toDTO)
                .toList();

        String fileName = winningMovies.isEmpty()
                ? "desconhecido"
                : winningMovies.get(0).fileName();

        log.info("MPA-FWM-00 | Vitórias consultadas no banco | fileName={} winningMovieCount={}",
                fileName, winningMovies.size());

        return winningMovies;
    }

    @Override
    @Transactional
    public void replaceAll(List<MovieDTO> movies) {
        String fileName = movies.isEmpty() ? "desconhecido" : movies.get(0).fileName();

        log.info("MPA-RA-00 | Iniciando substituição dos filmes | fileName={} movieCount={}",
                fileName, movies.size());

        movieJpaRepository.deleteAllInBatch();

        movieJpaRepository.saveAll(movies.stream().map(MovieEntity::new).toList());

        log.info("MPA-RA-01 | Filmes persistidos | fileName={} movieCount={}",
                fileName, movies.size());
    }
}
