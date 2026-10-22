package com.outsera.awards.movie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "release_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String studios;

    @Column(nullable = false, length = 2000)
    private String producers;

    private boolean winner;

    protected Movie() {
    }

    public Movie(int year, String title, String studios, String producers, boolean winner) {
        this.year = year;
        this.title = title;
        this.studios = studios;
        this.producers = producers;
        this.winner = winner;
    }

    public int getYear() {
        return year;
    }

    public String getProducers() {
        return producers;
    }

    public boolean isWinner() {
        return winner;
    }
}
