package com.deliverXY.backend.NewCode.analytics.dto;

import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import lombok.Data;

@Data
public class TripDTO {
    private Long id;
    private String title;
    private String pickupAddress;
    private String dropoffAddress;
    private String status;
    private Double distanceKm;
    private Double finalPrice;
    private Long driverId;

    public static TripDTO fromEntity(Delivery d) {
        TripDTO dto = new TripDTO();
        dto.setId(d.getId());
        dto.setTitle(d.getTitle());
        dto.setPickupAddress(d.getPickupAddress());
        dto.setDropoffAddress(d.getDropoffAddress());
        dto.setStatus(d.getStatus().name());
        dto.setDistanceKm(d.getDistanceKm());

        dto.setFinalPrice(
                d.getDeliveryPayment() != null ?
                        d.getDeliveryPayment().getFinalAmount().doubleValue() : 0.0
        );

        dto.setDriverId(
                d.getAgent() != null ? d.getAgent().getId() : null
        );

        return dto;
    }
}
