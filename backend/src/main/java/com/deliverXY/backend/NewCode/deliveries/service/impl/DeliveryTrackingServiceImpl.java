package com.deliverXY.backend.NewCode.deliveries.service.impl;

import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryTracking;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryTrackingRepository;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryTrackingServiceImpl implements DeliveryTrackingService {

    private final DeliveryTrackingRepository repo;
    private final DeliveryRepository deliveryRepo;
    private final LocationService locationService;

    @Override
    @Transactional
    public DeliveryTracking updateLocation(Long deliveryId, Double lat, Double lon) {
        var delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        DeliveryTracking t = repo.findById(deliveryId)
                .orElse(new DeliveryTracking());

        t.setDelivery(delivery);
        t.setCurrentLatitude(lat);
        t.setCurrentLongitude(lon);

        double distance = locationService.distanceKm(
                lat, lon,
                delivery.getDropoffLatitude(),
                delivery.getDropoffLongitude()
        );

        t.setEstimatedDistance(distance);
        t.setEstimatedDuration(locationService.calculateETA(distance, 35));
        t.setLastLocationUpdate(java.time.LocalDateTime.now());

        return repo.save(t);
    }

    @Override
    public DeliveryTracking getTracking(Long deliveryId) {
        return repo.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Tracking not found"));
    }
}