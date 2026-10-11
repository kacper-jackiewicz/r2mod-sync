package dev.kj.r2mod_sync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorsConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService serverProfileExecutor() {
        return Executors.newSingleThreadExecutor(task -> new Thread(task, "server-profile-operation"));
    }
}
