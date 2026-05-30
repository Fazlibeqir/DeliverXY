package com.deliverXY.backend.NewCode.deliveries.service.impl;

import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryTracking;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryTrackingRepository;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryTrackingService;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DeliveryTrackingServiceImpl implements DeliveryTrackingService {

    private final DeliveryTrackingRepository repo;
    private final DeliveryRepository deliveryRepo;
    private final LocationService locationService;

    @Override
    @Transactional
    public DeliveryTracking updateLocation(@NonNull Long deliveryId, Double lat, Double lon) {
        var delivery = Objects.requireNonNull(
                deliveryRepo.findById(deliveryId)
                        .orElseThrow(() -> new NotFoundException("Delivery not found: " + deliveryId))
        );

        DeliveryTracking t = repo.findById(deliveryId).orElse(new DeliveryTracking());

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
    @Transactional(readOnly = true)
    public DeliveryTracking getTracking(@NonNull Long deliveryId) {
        return Objects.requireNonNull(
                repo.findById(deliveryId)
                        .orElseThrow(() -> new NotFoundException("Tracking not found for delivery: " + deliveryId))
        );
    }
}
