package com.outsera.awards.config;

import com.outsera.awards.application.port.in.CalculateProducerIntervalsUseCase;
import com.outsera.awards.application.port.out.MovieCatalogPort;
import com.outsera.awards.application.service.CalculateProducerIntervalsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public CalculateProducerIntervalsUseCase calculateProducerIntervalsUseCase(
            MovieCatalogPort movieCatalog) {
        return new CalculateProducerIntervalsService(movieCatalog);
    }
}
