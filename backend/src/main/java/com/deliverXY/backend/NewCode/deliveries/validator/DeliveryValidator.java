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
        if (dto.getDropoffAddress() == null || dto.getDropoffAddress().isBlank()) {
            throw new ValidationException("Dropoff address is required");
        }

    }
}
