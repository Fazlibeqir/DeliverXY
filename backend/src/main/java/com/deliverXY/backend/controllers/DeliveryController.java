package com.deliverXY.backend.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeliveryController {

    @CrossOrigin(origins = "*")
    @GetMapping("/api/deliveries")
    public List<String> getDeliveries() {
        return List.of("Package 1", "Food Order 2", "Documents 3");
    }
}