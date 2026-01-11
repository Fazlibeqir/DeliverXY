package com.deliverXY.backend.NewCode.vehicle.service;

import com.deliverXY.backend.NewCode.vehicle.dto.VehicleRequestDTO;
import com.deliverXY.backend.NewCode.vehicle.dto.VehicleResponseDTO;

import java.util.List;

public interface VehicleService {

    VehicleResponseDTO create(Long userId, VehicleRequestDTO dto);

    VehicleResponseDTO update(Long userId, Long id, VehicleRequestDTO dto);

    void delete(Long userId, Long id);

    VehicleResponseDTO getById(Long id);

    List<VehicleResponseDTO> listMyVehicles(Long userId);
}
