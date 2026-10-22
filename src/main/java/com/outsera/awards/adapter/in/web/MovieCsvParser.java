package com.outsera.awards.adapter.in.web;

import com.outsera.awards.domain.model.Movie;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MovieCsvParser {

    private static final Set<String> REQUIRED_HEADERS =
            Set.of("year", "title", "studios", "producers", "winner");

    public List<Movie> parse(MultipartFile file) {
        List<Movie> movies = new ArrayList<>();
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
                Map<String, Integer> headers = parser.getHeaderMap();
                if (headers == null || !headers.keySet().containsAll(REQUIRED_HEADERS)) {
                    throw badCsv(
                            "O CSV deve conter as colunas year, title, studios, producers e winner.");
                }

                for (CSVRecord record : parser) {
                    if (record.size() != headers.size()) {
                        throw badCsv("O CSV contém uma linha com quantidade inválida de colunas.");
                    }
                    movies.add(parseMovie(record));
                }
            }
        } catch (IOException exception) {
            throw badCsv("O arquivo CSV está malformado ou não pôde ser lido.");
        } catch (NumberFormatException exception) {
            throw badCsv("A coluna year deve conter um ano numérico válido.");
        } catch (IllegalArgumentException exception) {
            throw badCsv("O CSV contém uma linha ou cabeçalho inválido.");
        }

        if (movies.isEmpty()) {
            throw badCsv("O CSV não contém registros de filmes.");
        }
        return movies;
    }

    private Movie parseMovie(CSVRecord record) {
        int year = Integer.parseInt(record.get("year").trim());
        String title = record.get("title").trim();
        String studios = record.get("studios").trim();
        String producers = record.get("producers").trim();
        String winnerValue = record.get("winner").trim();

        if (year < 1 || title.isEmpty() || studios.isEmpty() || producers.isEmpty()) {
            throw badCsv("Cada registro deve informar year, title, studios e producers.");
        }
        if (!winnerValue.isEmpty()
                && !winnerValue.equalsIgnoreCase("yes")
                && !winnerValue.equalsIgnoreCase("no")) {
            throw badCsv("A coluna winner deve conter yes, no ou ficar vazia.");
        }
        return new Movie(
                year, title, studios, producers, "yes".equalsIgnoreCase(winnerValue));
    }

    private UploadException badCsv(String message) {
        return new UploadException(HttpStatus.BAD_REQUEST, message);
    }
}
