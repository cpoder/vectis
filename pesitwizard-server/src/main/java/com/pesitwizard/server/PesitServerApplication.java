package com.pesitwizard.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.pesitwizard.security.SecretsConfig;

/**
 * PeSIT Server Application
 * Implements PeSIT Hors-SIT profile over TCP/IP
 */
@SpringBootApplication
@Import(SecretsConfig.class)
@EnableScheduling
public class PesitServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PesitServerApplication.class, args);
    }
}
