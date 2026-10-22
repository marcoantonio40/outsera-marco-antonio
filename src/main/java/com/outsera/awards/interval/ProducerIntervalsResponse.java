package com.outsera.awards.interval;

import java.util.List;

public record ProducerIntervalsResponse(
        List<ProducerInterval> min,
        List<ProducerInterval> max) {
}
