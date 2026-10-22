package com.outsera.awards.domain.model;

public record Movie(
        int year,
        String title,
        String studios,
        String producers,
        boolean winner) {
}
