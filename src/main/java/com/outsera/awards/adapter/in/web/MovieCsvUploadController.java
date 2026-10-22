package com.outsera.awards.adapter.in.web;

import com.outsera.awards.application.port.in.ImportMoviesUseCase;
import com.outsera.awards.domain.model.Movie;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/awards")
@Tag(name = "Awards", description = "Importação de filmes para cálculo dos intervalos")
public class MovieCsvUploadController {

    private final MovieCsvParser movieCsvParser;
    private final ImportMoviesUseCase importMovies;

    public MovieCsvUploadController(
            MovieCsvParser movieCsvParser, ImportMoviesUseCase importMovies) {
        this.movieCsvParser = movieCsvParser;
        this.importMovies = importMovies;
    }

    @PostMapping("/upload")
    @Operation(
            summary = "Enviar CSV e calcular intervalos",
            description = "Importa um CSV separado por ponto e vírgula com as colunas "
                    + "year, title, studios, producers e winner. O arquivo válido substitui "
                    + "a importação anterior e retorna os intervalos calculados.")
    public ProducerIntervalsResponse upload(
            @RequestPart(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new UploadException(HttpStatus.BAD_REQUEST, "Selecione um arquivo CSV não vazio.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new UploadException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie um arquivo com extensão .csv.");
        }

        List<Movie> movies = movieCsvParser.parse(file);
        return ProducerIntervalsResponse.from(importMovies.importMovies(movies));
    }
}
