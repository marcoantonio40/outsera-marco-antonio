package com.outsera.awards;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.outsera.awards.adapter.out.persistence.MovieJpaRepository;
import com.outsera.awards.adapter.out.persistence.entity.MovieEntity;
import com.outsera.awards.application.dto.MovieDTO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.csv.input-directory=target/test-csv-input")
@AutoConfigureMockMvc
class ProducerIntervalIntegrationTest {

    private static final Path CSV_DIRECTORY = Path.of("target/test-csv-input");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieJpaRepository movieRepository;

    @Test
    void redirectsRootToSwaggerUi() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/swagger-ui/index.html"));
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/awards/process']").exists())
                .andExpect(jsonPath("$.paths", aMapWithSize(1)))
                .andExpect(jsonPath("$.paths['/api/v1/awards/process'].post").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/awards/process'].post.requestBody").doesNotExist());
        mockMvc.perform(get("/api/v1/awards/intervals"))
                .andExpect(status().isNotFound());
    }

    @Test
    void processesCsvAndReturnsTheShortestAndLongestConsecutiveWinIntervals() throws Exception {
        org.assertj.core.api.Assertions.assertThat(movieRepository.count()).isZero();
        try (var csv = getClass().getResourceAsStream("/Movielist.csv")) {
            Files.write(CSV_DIRECTORY.resolve("Movielist.csv"), csv.readAllBytes());
        }

        mockMvc.perform(post("/api/v1/awards/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.min", hasSize(1)))
                .andExpect(jsonPath("$.min[0].producer").value("Joel Silver"))
                .andExpect(jsonPath("$.min[0].interval").value(1))
                .andExpect(jsonPath("$.min[0].previousWin").value(1990))
                .andExpect(jsonPath("$.min[0].followingWin").value(1991))
                .andExpect(jsonPath("$.max", hasSize(1)))
                .andExpect(jsonPath("$.max[0].producer").value("Matthew Vaughn"))
                .andExpect(jsonPath("$.max[0].interval").value(13))
                .andExpect(jsonPath("$.max[0].previousWin").value(2002))
                .andExpect(jsonPath("$.max[0].followingWin").value(2015));

        org.assertj.core.api.Assertions.assertThat(movieRepository.count()).isEqualTo(206);
        org.assertj.core.api.Assertions.assertThat(movieRepository.findAll())
                .allSatisfy(movie -> org.assertj.core.api.Assertions.assertThat(movie.getFileName())
                        .isEqualTo("Movielist.csv"));
        org.assertj.core.api.Assertions.assertThat(CSV_DIRECTORY.resolve("Movielist.csv")).exists();
    }

    @Test
    void rejectsInvalidCsvWithoutChangingPreviouslyImportedMovies() throws Exception {
        movieRepository.save(new MovieEntity(
                new MovieDTO(
                        2000, "Film", "Studio", "Producer", true, "previous.csv")));
        writeCsv("invalid.csv", "year;title\n2000;Film");

        mockMvc.perform(post("/api/v1/awards/process"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "O CSV deve conter as colunas year, title, studios, producers e winner."));
        org.assertj.core.api.Assertions.assertThat(movieRepository.count()).isEqualTo(1);
    }

    @Test
    void readsHeadersWithoutCaseSensitivityAndOnlyYesMarksWinner() throws Exception {
        String csv = "YEAR;TiTle;STUDIOS;Producers;WINNER\n"
                + "2000;Film Yes;Studio;Producer;Yes\n"
                + "2001;Film Lowercase;Studio;Producer;yes\n"
                + "2002;Film Other;Studio;Producer;anything\n"
                + "2003;Film Empty;Studio;Producer;\n";
        writeCsv("movies.csv", csv);

        mockMvc.perform(post("/api/v1/awards/process"))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(movieRepository.findAll())
                .extracting(movie -> movie.toDTO().winner())
                .containsExactlyInAnyOrder(true, true, false, false);
    }

    @Test
    void processesOnlyTheMostRecentlyModifiedCsv() throws Exception {
        writeCsv("older.csv", "year;title;studios;producers;winner\n2000;Older;Studio;Producer;yes");
        writeCsv("newer.CSV", "year;title;studios;producers;winner\n2001;Newer;Studio;Producer;yes");
        Files.setLastModifiedTime(CSV_DIRECTORY.resolve("older.csv"),
                FileTime.from(Instant.parse("2020-01-01T00:00:00Z")));
        Files.setLastModifiedTime(CSV_DIRECTORY.resolve("newer.CSV"),
                FileTime.from(Instant.parse("2021-01-01T00:00:00Z")));

        mockMvc.perform(post("/api/v1/awards/process"))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(movieRepository.count()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(movieRepository.findAll().get(0).getFileName())
                .isEqualTo("newer.CSV");
        org.assertj.core.api.Assertions.assertThat(CSV_DIRECTORY.resolve("newer.CSV")).exists();
    }

    @Test
    void returnsNotFoundWhenThereIsNoCsvInTheFolder() throws Exception {
        writeCsv("notes.txt", "not a csv");

        mockMvc.perform(post("/api/v1/awards/process"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Nenhum arquivo CSV foi encontrado na pasta de processamento."));
    }

    @BeforeEach
    void clearState() throws IOException {
        movieRepository.deleteAllInBatch();
        Files.createDirectories(CSV_DIRECTORY);
        try (var paths = Files.list(CSV_DIRECTORY)) {
            for (Path path : paths.toList()) {
                Files.delete(path);
            }
        }
    }

    private void writeCsv(String fileName, String content) throws IOException {
        Files.writeString(CSV_DIRECTORY.resolve(fileName), content);
    }
}
