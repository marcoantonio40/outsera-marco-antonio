package com.outsera.awards.adapter.in.web;

import com.outsera.awards.domain.dto.MovieDTO;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class MovieCsvParser {

    private static final Set<String> REQUIRED_HEADERS =
            Set.of("year", "title", "studios", "producers", "winner");

    public List<MovieDTO> parse(MultipartFile file, String fileName) {
        log.info("MCP-PAR-00 | Iniciando leitura do CSV | fileName={}", fileName);
        List<MovieDTO> movies = new ArrayList<>();
        try (PushbackReader reader = new PushbackReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), 1)) {
            int firstCharacter = reader.read();

            if (firstCharacter != '\uFEFF' && firstCharacter != -1) {
                reader.unread(firstCharacter);
            }

            try (CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setDelimiter(';')
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .build()
                    .parse(reader)) {

                Map<String, Integer> headers = new HashMap<>();
                parser.getHeaderMap().forEach(
                        (header, index) -> headers.put(header.toLowerCase(Locale.ROOT), index));

                if (!headers.keySet().containsAll(REQUIRED_HEADERS)) {
                    throw badCsv(
                            "O CSV deve conter as colunas year, title, studios, producers e winner.");
                }

                for (CSVRecord record : parser) {
                    if (record.size() != headers.size()) {
                        throw badCsv("O CSV contém uma linha com quantidade inválida de colunas.");
                    }
                    movies.add(parseMovie(record, headers, fileName));
                }
            }
        } catch (IOException exception) {
            log.error("MCP-PAR-01 | Falha de leitura do CSV | fileName={} reason={}",
                    fileName, exception.getMessage());
            throw badCsv("O arquivo CSV está malformado ou não pôde ser lido.");
        } catch (UploadException exception) {
            log.error("MCP-PAR-02 | CSV rejeitado | fileName={} reason={}",
                    fileName, exception.getMessage());
            throw exception;
        } catch (NumberFormatException exception) {
            log.error("MCP-PAR-03 | Ano inválido no CSV | fileName={} reason={}",
                    fileName, exception.getMessage());
            throw badCsv("A coluna year deve conter um ano numérico válido.");
        } catch (IllegalArgumentException exception) {
            log.error("MCP-PAR-04 | Estrutura inválida no CSV | fileName={} reason={}",
                    fileName, exception.getMessage());
            throw badCsv("O CSV contém uma linha ou cabeçalho inválido.");
        }

        if (movies.isEmpty()) {
            log.error("MCP-PAR-05 | CSV sem registros | fileName={}", fileName);
            throw badCsv("O CSV não contém registros de filmes.");
        }
        log.info("MCP-PAR-06 | Leitura do CSV concluída | fileName={} movieCount={}",
                fileName, movies.size());
        return movies;
    }

    private MovieDTO parseMovie(CSVRecord record, Map<String, Integer> headers, String fileName) {
        int year = Integer.parseInt(record.get(headers.get("year")).trim());
        String title = record.get(headers.get("title")).trim();
        String studios = record.get(headers.get("studios")).trim();
        String producers = record.get(headers.get("producers")).trim();
        String winnerValue = record.get(headers.get("winner")).trim();

        if (year < 1 || title.isEmpty() || studios.isEmpty() || producers.isEmpty()) {
            throw badCsv("Cada registro deve informar year, title, studios e producers.");
        }
        return new MovieDTO(
                year, title, studios, producers, "yes".equalsIgnoreCase(winnerValue), fileName);
    }

    private UploadException badCsv(String message) {
        return new UploadException(HttpStatus.BAD_REQUEST, message);
    }
}
