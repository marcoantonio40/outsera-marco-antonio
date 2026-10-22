package com.outsera.awards.domain.model;

public record ProducerInterval(
        String producer,
        int interval,
        int previousWin,
        int followingWin) {
}
