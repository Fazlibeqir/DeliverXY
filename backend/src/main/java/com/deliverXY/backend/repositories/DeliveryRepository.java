package com.deliverXY.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.deliverXY.backend.models.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery,Long> {

    
}