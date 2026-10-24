package com.outsera.awards.domain.dto;

import java.util.List;

public record ProducerIntervalsDTO(
        List<ProducerIntervalDTO> min,
        List<ProducerIntervalDTO> max) {
}
