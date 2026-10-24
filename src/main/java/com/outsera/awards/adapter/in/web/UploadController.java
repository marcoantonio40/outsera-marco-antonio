package com.outsera.awards.adapter.in.web;

import com.outsera.awards.application.port.in.ImportMoviesUseCase;
import com.outsera.awards.adapter.in.web.request.MovieUploadRequest;
import com.outsera.awards.adapter.in.web.response.ProducerIntervalsResponse;
import com.outsera.awards.domain.dto.MovieDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

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
            @ModelAttribute MovieUploadRequest request) {
        var file = request.file();

        if (file == null || file.isEmpty()) {
            log.error("UPC-UP-00 | Upload rejeitado: arquivo ausente ou vazio");
            throw new UploadException(HttpStatus.BAD_REQUEST, "Selecione um arquivo CSV não vazio.");
        }

        String filename = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "arquivo-desconhecido";

        Map<String, Object> fileMetadata = Map.of(
                "fileName", filename,
                "sizeBytes", file.getSize());
        log.info("UPC-UP-01 | Início do processamento do upload | {}", fileMetadata);

        if (!filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            log.error("UPC-UP-02 | Upload rejeitado: extensão de arquivo inválida | fileName={}", filename);
            throw new UploadException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie um arquivo com extensão .csv.");
        }

        log.info("UPC-UP-03 | Iniciando parse do CSV | fileName={}", filename);

        List<MovieDTO> movies = movieCsvParser.parse(file, filename);
        log.info("UPC-UP-04 | Parse do CSV concluído | fileName={} movieCount={}",
                filename, movies.size());

        ProducerIntervalsResponse response =
                ProducerIntervalsResponse.from(importMovies.importMovies(movies));
        log.info("UPC-UP-05 | Upload processado com sucesso | fileName={} movieCount={} minCount={} maxCount={}",
                filename, movies.size(), response.min().size(), response.max().size());
        return response;
    }
}
