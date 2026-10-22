package com.outsera.awards.interval;

public record ProducerInterval(
        String producer,
        int interval,
        int previousWin,
        int followingWin) {
}
