package com.outsera.awards.adapter.in.web;

public record ProducerIntervalResponse(
        String producer,
        int interval,
        int previousWin,
        int followingWin) {
}
