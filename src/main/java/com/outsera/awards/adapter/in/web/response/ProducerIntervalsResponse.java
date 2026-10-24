package com.outsera.awards.adapter.in.web.response;

import com.outsera.awards.domain.dto.ProducerIntervalsDTO;
import java.util.List;

public record ProducerIntervalsResponse(
        List<ProducerIntervalResponse> min,
        List<ProducerIntervalResponse> max) {

    public static ProducerIntervalsResponse from(ProducerIntervalsDTO intervals) {
        return new ProducerIntervalsResponse(
                intervals.min().stream()
                        .map(interval -> new ProducerIntervalResponse(
                                interval.producer(),
                                interval.interval(),
                                interval.previousWin(),
                                interval.followingWin()))
                        .toList(),
                intervals.max().stream()
                        .map(interval -> new ProducerIntervalResponse(
                                interval.producer(),
                                interval.interval(),
                                interval.previousWin(),
                                interval.followingWin()))
                        .toList());
    }
}
