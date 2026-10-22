package com.outsera.awards.adapter.in.web;

import com.outsera.awards.application.port.in.CalculateProducerIntervalsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/awards")
@Tag(name = "Awards", description = "Consulta dos intervalos entre prêmios dos produtores")
public class ProducerIntervalController {

    private final CalculateProducerIntervalsUseCase calculateProducerIntervals;

    public ProducerIntervalController(CalculateProducerIntervalsUseCase calculateProducerIntervals) {
        this.calculateProducerIntervals = calculateProducerIntervals;
    }

    @GetMapping("/intervals")
    @Operation(
            summary = "Consultar intervalos",
            description = "Retorna os menores e maiores intervalos entre vitórias consecutivas "
                    + "dos produtores do arquivo CSV importado.")
    public ProducerIntervalsResponse getProducerIntervals() {
        return ProducerIntervalsResponse.from(calculateProducerIntervals.calculate());
    }
}
