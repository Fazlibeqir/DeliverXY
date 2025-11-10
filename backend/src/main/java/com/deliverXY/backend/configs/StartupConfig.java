package com.deliverXY.backend.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class StartupConfig {

    @Bean
    public CommandLineRunner commandLineRunner(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return args -> {
            System.out.println("=== DELIVERXY BACKEND STARTUP ===");
            System.out.println("Active endpoints:");
            requestMappingHandlerMapping.getHandlerMethods().forEach((key, value) -> {
                System.out.println("  " + key + " -> " + value);
            });
            System.out.println("=== STARTUP COMPLETE ===");
        };
    }
} 