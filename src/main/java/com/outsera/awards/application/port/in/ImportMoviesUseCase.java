package com.outsera.awards.application.port.in;

import com.outsera.awards.domain.dto.MovieDTO;
import com.outsera.awards.domain.dto.ProducerIntervalsDTO;
import java.util.List;

public interface ImportMoviesUseCase {

    ProducerIntervalsDTO importMovies(List<MovieDTO> movies);
}
