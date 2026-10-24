package com.outsera.awards.application.dto;

public record ProducerIntervalDTO(
        String producer,
        int interval,
        int previousWin,
        int followingWin) {
}
