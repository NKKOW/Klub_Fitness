package org.example.klubfitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("org.example.klubfitness.entity")
@EnableJpaRepositories("org.example.klubfitness.repository")
public class KlubFitnessApplication {

    public static void main(String[] args) {
        SpringApplication.run(KlubFitnessApplication.class, args);
    }
}
