package com.outsera.awards.application.dto;

public record MovieDTO(
        int year,
        String title,
        String studios,
        String producers,
        boolean winner,
        String fileName) {
}
