package com.outsera.awards.adapter.in.web;

import com.outsera.awards.application.port.in.CalculateProducerIntervalsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/awards")
public class ProducerIntervalController {

    private final CalculateProducerIntervalsUseCase calculateProducerIntervals;

    public ProducerIntervalController(CalculateProducerIntervalsUseCase calculateProducerIntervals) {
        this.calculateProducerIntervals = calculateProducerIntervals;
    }

    @GetMapping("/intervals")
    public ProducerIntervalsResponse getProducerIntervals() {
        return ProducerIntervalsResponse.from(calculateProducerIntervals.calculate());
    }
}
