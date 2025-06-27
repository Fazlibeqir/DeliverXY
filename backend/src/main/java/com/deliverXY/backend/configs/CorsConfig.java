package com.deliverXY.backend.configs;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CorsFilter;

//import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  //  @Value("${app.cors.allowed-origins}")
    //private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
       
        CorsConfiguration config=new CorsConfiguration();
        config.setAllowCredentials(true);
       // List<String> origins = Arrays.asList(allowedOrigins.split(","));
       
        config.setAllowedOrigins(Arrays.asList("http://13.48.56.2:3000", "http://ec2-13-48-56-2.eu-north-1.compute.amazonaws.com:3000"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
