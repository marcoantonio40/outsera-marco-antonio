package com.outsera.awards.application.dto;

import java.util.List;

public record ProducerIntervalsDTO(
        List<ProducerIntervalDTO> min,
        List<ProducerIntervalDTO> max) {
}
