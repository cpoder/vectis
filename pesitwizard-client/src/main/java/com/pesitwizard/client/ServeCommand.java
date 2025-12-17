package com.pesitwizard.client;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command to run the PeSIT client as a REST API server
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Command(name = "serve", description = "Run as REST API server", mixinStandardHelpOptions = true)
public class ServeCommand implements Callable<Integer> {

    private final ApplicationContext applicationContext;

    @Option(names = { "-p", "--port" }, description = "Server port (default: 8080)")
    private Integer port = 8080;

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    @Override
    public Integer call() {
        log.info("PeSIT Client REST API server started on port {}", port);
        log.info("API endpoints available at http://localhost:{}/api/v1/", port);
        log.info("Health check: http://localhost:{}/actuator/health", port);
        log.info("H2 Console: http://localhost:{}/h2-console", port);
        log.info("Press Ctrl+C to stop...");

        // Signal that the application is ready to accept traffic
        AvailabilityChangeEvent.publish(applicationContext, ReadinessState.ACCEPTING_TRAFFIC);
        log.info("Readiness state set to ACCEPTING_TRAFFIC");

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            shutdownLatch.countDown();
        }));

        try {
            // Keep the application running
            shutdownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return 0;
    }
}
