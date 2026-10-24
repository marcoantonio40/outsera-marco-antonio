package com.outsera.awards.domain.dto;

public record ProducerIntervalDTO(
        String producer,
        int interval,
        int previousWin,
        int followingWin) {
}
