package com.deliverXY.backend.NewCode.user.validator;

import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class KYCValidationService {

    public void validateKYCFiles(String front, String back, String selfie) {
        if (front == null || back == null || selfie == null) {
            throw new BadRequestException("KYC documents incomplete");
        }
    }
}
