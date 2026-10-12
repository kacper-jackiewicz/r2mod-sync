package dev.kj.r2mod_sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class R2modSyncApplication {

    static void main(String[] args) {
        SpringApplication.run(R2modSyncApplication.class, args);
    }

}
