package kos.progs.telegram.bot.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"kos.progs.telegram.bot"})
@EnableJpaRepositories(basePackages = {"kos.progs.telegram.bot"})
public class StarterRunnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarterRunnerApplication.class, args);
    }
}
