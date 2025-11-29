package com.deliverXY.backend.NewCode.rating.dto;

import lombok.Data;

@Data
public class RatingRequestDTO {
    private Long deliveryId;
    private int rating;
    private String review;
}
