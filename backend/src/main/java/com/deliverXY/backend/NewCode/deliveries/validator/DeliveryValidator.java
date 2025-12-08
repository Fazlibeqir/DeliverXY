package com.deliverXY.backend.NewCode.deliveries.validator;

import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryDTO;
import org.springframework.stereotype.Component;

@Component
public class DeliveryValidator {

    public void validateCreate(DeliveryDTO dto) {

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new ValidationException("Title is required.");
        }

        if (dto.getPickupAddress() == null) {
            throw new ValidationException("Pickup address is required.");
        }

        if (dto.getDropoffAddress() == null) {
            throw new ValidationException("Dropoff address is required.");
        }


        if (dto.getPickupLatitude() == null || dto.getPickupLongitude() == null) {
            throw new ValidationException("Pickup location is required.");
        }

        if (dto.getDropoffLatitude() == null || dto.getDropoffLongitude() == null) {
            throw new ValidationException("Dropoff location is required.");
        }
    }
}
