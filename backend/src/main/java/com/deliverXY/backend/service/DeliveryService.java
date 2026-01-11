package com.deliverXY.backend.service;

import java.util.List;

import com.deliverXY.backend.models.Delivery;

public interface DeliveryService {
    List<Delivery> findAll();
    Delivery findById(Long id);
    Delivery create(Delivery delivery);
    Delivery update(Long id, Delivery delivery);
    void delete(Long id);
}
