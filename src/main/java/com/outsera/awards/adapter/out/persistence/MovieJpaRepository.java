package com.outsera.awards.adapter.out.persistence;

import com.outsera.awards.adapter.out.persistence.entity.MovieEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieJpaRepository extends JpaRepository<MovieEntity, Long> {

    List<MovieEntity> findByWinnerTrueOrderByYearAsc();
}
