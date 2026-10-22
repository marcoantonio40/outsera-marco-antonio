package com.outsera.awards.domain.model;

import java.util.List;

public record ProducerIntervals(
        List<ProducerInterval> min,
        List<ProducerInterval> max) {
}
