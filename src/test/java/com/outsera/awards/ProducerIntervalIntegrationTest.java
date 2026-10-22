package com.outsera.awards;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.outsera.awards.adapter.out.persistence.MovieJpaRepository;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProducerIntervalIntegrationTest {

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
                .andExpect(jsonPath("$.paths['/api/v1/awards/upload']").exists())
                .andExpect(jsonPath("$.paths", aMapWithSize(1)))
                .andExpect(jsonPath("$.paths['/api/v1/awards/upload'].post").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/awards/upload'].post.requestBody.content['multipart/form-data']"
                                + ".schema.properties.file.type").value("string"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/awards/upload'].post.requestBody.content['multipart/form-data']"
                                + ".schema.properties.file.format").value("binary"));
        mockMvc.perform(get("/api/v1/awards/intervals"))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadsCsvAndReturnsTheShortestAndLongestConsecutiveWinIntervals() throws Exception {
        org.assertj.core.api.Assertions.assertThat(movieRepository.count()).isZero();
        try (InputStream csv = getClass().getResourceAsStream("/Movielist.csv")) {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "Movielist.csv", "text/csv", csv);

            mockMvc.perform(multipart("/api/v1/awards/upload").file(file))
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
        }
    }

    @Test
    void rejectsInvalidCsvWithoutChangingPreviouslyImportedMovies() throws Exception {
        movieRepository.save(new com.outsera.awards.adapter.out.persistence.MovieEntity(
                new com.outsera.awards.domain.model.Movie(2000, "Film", "Studio", "Producer", true)));
        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.csv", "text/csv", "year;title\n2000;Film".getBytes());

        mockMvc.perform(multipart("/api/v1/awards/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "O CSV deve conter as colunas year, title, studios, producers e winner."));
        org.assertj.core.api.Assertions.assertThat(movieRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsFilesWithoutCsvExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "movies.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/awards/upload").file(file))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.message").value("Envie um arquivo com extensão .csv."));
    }

    @BeforeEach
    void clearMovies() {
        movieRepository.deleteAllInBatch();
    }
}
