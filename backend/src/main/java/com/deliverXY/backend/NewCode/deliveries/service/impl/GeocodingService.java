package com.deliverXY.backend.NewCode.deliveries.service.impl;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public GeoPoint geocode(String address, String city) {
        try {
            String query = URLEncoder.encode(address + ", " + city, StandardCharsets.UTF_8);
            String url =
                    "https://nominatim.openstreetmap.org/search" +
                            "?format=json&limit=1&q=" + query;

            ResponseEntity<List<Map<String, Object>>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            new HttpEntity<>(new HttpHeaders()),
                            new ParameterizedTypeReference<>() {}
                    );

            if (response.getBody() == null || response.getBody().isEmpty()) {
                throw new IllegalArgumentException("Address not found");
            }

            Map<String, Object> r = response.getBody().get(0);

            return new GeoPoint(
                    Double.parseDouble((String) r.get("lat")),
                    Double.parseDouble((String) r.get("lon"))
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to geocode address");
        }
    }

    public record GeoPoint(double lat, double lng) {}
}

