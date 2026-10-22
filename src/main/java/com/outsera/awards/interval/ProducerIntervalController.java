package com.outsera.awards.interval;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/awards")
public class ProducerIntervalController {

    private final ProducerIntervalService producerIntervalService;

    public ProducerIntervalController(ProducerIntervalService producerIntervalService) {
        this.producerIntervalService = producerIntervalService;
    }

    @GetMapping("/intervals")
    public ProducerIntervalsResponse getProducerIntervals() {
        return producerIntervalService.calculate();
    }
}
