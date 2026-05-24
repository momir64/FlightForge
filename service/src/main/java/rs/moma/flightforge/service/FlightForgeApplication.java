package rs.moma.flightforge.service;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.kie.api.runtime.KieContainer;
import org.kie.api.KieServices;

@SpringBootApplication
@EntityScan(basePackages = "rs.moma.flightforge")
public class FlightForgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightForgeApplication.class, args);
    }

    @Bean
    public KieContainer kieContainer() {
        return KieServices.Factory.get().getKieClasspathContainer();
    }
}
