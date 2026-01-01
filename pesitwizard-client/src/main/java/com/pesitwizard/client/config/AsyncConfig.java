package com.pesitwizard.client.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async configuration for background transfer execution.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "transferExecutor")
    public Executor transferExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("transfer-");
        executor.initialize();
        return executor;
    }
}
