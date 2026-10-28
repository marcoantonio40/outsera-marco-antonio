package com.outsera.awards.adapter.in.web;

import com.outsera.awards.adapter.in.csv.CsvFolderProcessor;
import com.outsera.awards.adapter.in.web.response.ProducerIntervalsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/awards")
@Tag(name = "Awards", description = "Importação de filmes para cálculo dos intervalos")
public class AwardsProcessingController {

    private final CsvFolderProcessor csvFolderProcessor;

    @PostMapping("/process")
    @Operation(
            summary = "Processar o CSV mais recente da pasta",
            description = "Lê o arquivo CSV mais recente em arquivos_para_processar, "
                    + "substitui os filmes importados e calcula os intervalos.")
    public ProducerIntervalsResponse processLatestCsv() {
        ProducerIntervalsResponse response =
                ProducerIntervalsResponse.from(csvFolderProcessor.processLatestCsv());
        log.info("APC-PLC-00 | Resposta de processamento gerada | minCount={} maxCount={}",
                response.min().size(), response.max().size());
        return response;
    }
}
