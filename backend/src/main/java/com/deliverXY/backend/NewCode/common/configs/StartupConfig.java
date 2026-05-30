package com.deliverXY.backend.NewCode.common.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@Slf4j
public class StartupConfig {

    @Bean
    public CommandLineRunner commandLineRunner(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return args -> {
            log.info("=== DELIVERXY BACKEND STARTUP ===");
            log.info("Active endpoints:");
            requestMappingHandlerMapping.getHandlerMethods().forEach((key, value) ->
                    log.info("  {} -> {}", key, value));
            log.info("=== STARTUP COMPLETE ===");
        };
    }
}
