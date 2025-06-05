package com.deliverXY.backend.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.repositories.DeliveryRepository;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DeliveryController {
    private final DeliveryRepository deliveryRepository;

    public DeliveryController(DeliveryRepository deliveryRepository){
        this.deliveryRepository = deliveryRepository;
    }

    @GetMapping("/deliveries")
    public List<Delivery> getDeliveries() {
        return deliveryRepository.findAll();
    }
}