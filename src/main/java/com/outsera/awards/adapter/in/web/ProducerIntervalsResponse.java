package com.outsera.awards.adapter.in.web;

import com.outsera.awards.domain.model.ProducerIntervals;
import java.util.List;

public record ProducerIntervalsResponse(
        List<ProducerIntervalResponse> min,
        List<ProducerIntervalResponse> max) {

    public static ProducerIntervalsResponse from(ProducerIntervals intervals) {
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
