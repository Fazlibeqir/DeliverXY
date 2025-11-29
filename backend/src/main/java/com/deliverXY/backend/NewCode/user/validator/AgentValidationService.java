package com.deliverXY.backend.NewCode.user.validator;

import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class AgentValidationService {

    public void validateLocation(Double lat, Double lon) {
        if (lat == null || lon == null) {
            throw new BadRequestException("Location is invalid");
        }
    }
}
