package com.outsera.awards.adapter.in.web;

import com.outsera.awards.application.port.in.ImportMoviesUseCase;
import com.outsera.awards.domain.model.Movie;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/awards")
@Tag(name = "Awards", description = "Importação de filmes para cálculo dos intervalos")
public class UploadController {

    private final MovieCsvParser movieCsvParser;
    private final ImportMoviesUseCase importMovies;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Enviar CSV e calcular intervalos",
            description = "Importa um CSV separado por ponto e vírgula com as colunas "
                    + "year, title, studios, producers e winner.")
    public ProducerIntervalsResponse upload(
            @RequestPart(value = "file", required = false) MultipartFile file) {

        if (file == null || file.isEmpty()) {
            log.warn("UPC-UP-00", "Arquivo CSV não enviado ou vazio", file);
            throw new UploadException(HttpStatus.BAD_REQUEST, "Selecione um arquivo CSV não vazio.");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "arquivo desconhecido";

        log.info("UPC-UP-01", "Inicio do processamento do arquivo", Map.of("Nome do arquivo", filename, "Tamanho do arquivo", file.getSize()));

        if (!filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            log.warn("UPC-UP-02", "Arquivo no formato diferente de CSV", file);
            throw new UploadException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie um arquivo com extensão .csv.");


        }

        log.info("UPC-UP-03", "Inciar o parse do aqruivo", Map.of("Nome do arquivo", filename));

        List<Movie> movies = movieCsvParser.parse(file, filename);

        log.info("UPC-UP-04", "Inciar o parse do aqruivo", Map.of("Nome do arquivo", filename));

        return ProducerIntervalsResponse.from(importMovies.importMovies(movies));
    }
}
