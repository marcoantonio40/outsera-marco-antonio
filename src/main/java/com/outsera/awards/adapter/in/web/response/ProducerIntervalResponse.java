package com.outsera.awards.adapter.in.web.response;

public record ProducerIntervalResponse(
        String producer,
        int interval,
        int previousWin,
        int followingWin) {
}
