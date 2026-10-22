package com.outsera.awards.movie;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class MovieCsvImporter implements ApplicationRunner {

    private final MovieRepository movieRepository;

    public MovieCsvImporter(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        ClassPathResource resource = new ClassPathResource("Movielist.csv");
        List<Movie> movies = new ArrayList<>();

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {
            for (CSVRecord record : parser) {
                movies.add(new Movie(
                        Integer.parseInt(record.get("year").trim()),
                        record.get("title").trim(),
                        record.get("studios").trim(),
                        record.get("producers").trim(),
                        "yes".equalsIgnoreCase(record.get("winner").trim())));
            }
        }

        movieRepository.deleteAllInBatch();
        movieRepository.saveAll(movies);
    }
}
