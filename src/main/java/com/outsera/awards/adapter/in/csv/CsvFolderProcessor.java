package com.outsera.awards.adapter.in.csv;

import com.outsera.awards.adapter.in.web.exception.UploadException;
import com.outsera.awards.application.dto.ProducerIntervalsDTO;
import com.outsera.awards.application.port.in.ImportMoviesUseCase;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CsvFolderProcessor {

    private final MovieCsvParser movieCsvParser;
    private final ImportMoviesUseCase importMovies;

    @Value("${app.csv.input-directory:arquivos_para_processar}")
    private String inputDirectory;

    public ProducerIntervalsDTO processLatestCsv() {
        Path directory = Path.of(inputDirectory);
        Path latestCsv = findLatestCsv(directory);
        String fileName = latestCsv.getFileName().toString();

        log.info("CFP-PLC-00 | Iniciando processamento do CSV mais recente | fileName={} directory={}",
                fileName, directory.toAbsolutePath());

        var movies = movieCsvParser.parse(latestCsv);
        log.info("CFP-PLC-01 | Parse do CSV concluído | fileName={} movieCount={}",
                fileName, movies.size());

        ProducerIntervalsDTO result = importMovies.importMovies(movies);
        log.info("CFP-PLC-02 | CSV processado | fileName={} movieCount={}",
                fileName, movies.size());
        return result;
    }

    private Path findLatestCsv(Path directory) {
        try {
            Files.createDirectories(directory);
            List<Path> csvFiles;
            try (Stream<Path> paths = Files.list(directory)) {
                csvFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString()
                                .toLowerCase(Locale.ROOT).endsWith(".csv"))
                        .toList();
            }

            Path latest = null;
            FileTime latestModified = null;
            for (Path file : csvFiles) {
                FileTime modified = Files.getLastModifiedTime(file);
                if (latest == null
                        || modified.compareTo(latestModified) > 0
                        || (modified.equals(latestModified)
                        && Comparator.comparing(Path::toString).compare(file, latest) > 0)) {
                    latest = file;
                    latestModified = modified;
                }
            }

            if (latest == null) {
                throw new UploadException(
                        HttpStatus.NOT_FOUND,
                        "Nenhum arquivo CSV foi encontrado na pasta de processamento.");
            }
            return latest;
        } catch (IOException exception) {
            log.error("CFP-PLC-03 | Falha ao localizar CSV na pasta | directory={} reason={}",
                    directory.toAbsolutePath(), exception.getMessage());
            throw new UploadException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível acessar a pasta de processamento.");
        }
    }
}
