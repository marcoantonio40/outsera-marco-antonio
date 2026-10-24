package com.outsera.awards.application.port.in;

import com.outsera.awards.application.dto.MovieDTO;
import com.outsera.awards.application.dto.ProducerIntervalsDTO;
import java.util.List;

public interface ImportMoviesUseCase {

    ProducerIntervalsDTO importMovies(List<MovieDTO> movies);
}
