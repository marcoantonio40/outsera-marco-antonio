package com.outsera.awards.adapter.out.persistence.entity;

import com.outsera.awards.application.dto.MovieDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "movies")
public class MovieEntity {

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

    @Column(name = "file_name", nullable = false, length = 1024)
    private String fileName;

    protected MovieEntity() {
    }

    public MovieEntity(MovieDTO movieDTO) {
        this.year = movieDTO.year();
        this.title = movieDTO.title();
        this.studios = movieDTO.studios();
        this.producers = movieDTO.producers();
        this.winner = movieDTO.winner();
        this.fileName = movieDTO.fileName();
    }

    public MovieDTO toDTO() {
        return new MovieDTO(year, title, studios, producers, winner, fileName);
    }

    public String getFileName() {
        return fileName;
    }
}
