package com.deliverXY.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.service.DeliveryService;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final DeliveryService  svc;

    public DeliveryController(DeliveryService  deliveryRepository){
        this.svc = deliveryRepository;
    }

    @GetMapping
    public List<Delivery> getAll() { 
        return svc.findAll(); 
    }

    @GetMapping("/{id}") 
    public ResponseEntity<Delivery> getById(@PathVariable Long id) {
        Delivery d = svc.findById(id);
        return d != null ? ResponseEntity.ok(d) : ResponseEntity.notFound().build();
    }

    @PostMapping 
    public Delivery create(@RequestBody Delivery d) { 
        return svc.create(d);
     }

    @PutMapping("/{id}") 
    public ResponseEntity<Delivery> update(@PathVariable Long id, @RequestBody Delivery d){
        return ResponseEntity.ok(svc.update(id, d));
    }

    @DeleteMapping("/{id}") 
    public ResponseEntity<?> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.ok().build();
    }
}