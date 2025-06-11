package com.deliverXY.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.repositories.DeliveryRepository;
import com.deliverXY.backend.service.DeliveryService;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository repo;
    public DeliveryServiceImpl(DeliveryRepository repo) { this.repo = repo; }

    @Override public List<Delivery> findAll() { return repo.findAll(); }
    @Override public Delivery findById(Long id) { return repo.findById(id).orElse(null); }
    @Override public Delivery create(Delivery d) { return repo.save(d); }
    @Override public Delivery update(Long id, Delivery d) {
        Delivery existing = repo.findById(id).orElseThrow();
        existing.setDescription(d.getDescription());
        existing.setStatus(d.getStatus());
        return repo.save(existing);
    }
    @Override public void delete(Long id) { repo.deleteById(id); }

    
}
