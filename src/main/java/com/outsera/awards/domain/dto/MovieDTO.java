package com.outsera.awards.domain.dto;

public record MovieDTO(
        int year,
        String title,
        String studios,
        String producers,
        boolean winner,
        String fileName) {
}
