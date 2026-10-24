package com.outsera.awards.application.port.out;

import com.outsera.awards.application.dto.MovieDTO;
import java.util.List;

public interface MovieCatalogPort {

    List<MovieDTO> findWinningMovies();

    void replaceAll(List<MovieDTO> movies);
}
