package com.deliverXY.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverXY.backend.models.Delivery;


public interface DeliveryRepository extends JpaRepository<Delivery,Long> {

    
}