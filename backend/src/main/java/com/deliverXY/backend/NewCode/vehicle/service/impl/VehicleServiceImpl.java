package com.deliverXY.backend.NewCode.vehicle.service.impl;

import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.repository.AppUserRepository;
import com.deliverXY.backend.NewCode.vehicle.domain.Vehicle;
import com.deliverXY.backend.NewCode.vehicle.dto.VehicleRequestDTO;
import com.deliverXY.backend.NewCode.vehicle.dto.VehicleResponseDTO;
import com.deliverXY.backend.NewCode.vehicle.repository.VehicleRepository;
import com.deliverXY.backend.NewCode.vehicle.service.VehicleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository repo;
    private final AppUserRepository userRepo;

    private void checkUniqueLicensePlate(Long currentVehicleId, String newPlate){
        if (repo.existsByLicensePlate(newPlate)) {
            // Fetch potential vehicle with the same plate
            Vehicle existing = repo.findByLicensePlate(newPlate)
                    .orElse(null);

            // If found, check if it's a *different* vehicle
            if (existing != null && !Objects.equals(existing.getId(), currentVehicleId)) {
                throw new BadRequestException("License plate already registered for another vehicle");
            }
        }
    }

    @Override
    @Transactional
    public VehicleResponseDTO create(Long userId, VehicleRequestDTO dto) {

        checkUniqueLicensePlate(null, dto.getLicensePlate());

        AppUser owner = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Vehicle v = new Vehicle();
        v.setOwner(owner);

        map(v, dto);

        repo.save(v);
        return toDTO(v);
    }

    @Override
    @Transactional
    public VehicleResponseDTO update(Long userId, Long id, VehicleRequestDTO dto) {

        Vehicle v = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));

        if (!v.getOwner().getId().equals(userId))
            throw new BadRequestException("You do not own this vehicle");

        if (!v.getLicensePlate().equals(dto.getLicensePlate())) {
            checkUniqueLicensePlate(id, dto.getLicensePlate());
        }

        map(v, dto);
        repo.save(v);

        return toDTO(v);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Vehicle v = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));

        if (!v.getOwner().getId().equals(userId))
            throw new BadRequestException("You do not own this vehicle");

        repo.delete(v);
    }

    @Override
    public VehicleResponseDTO getById(Long id) {
        Vehicle v = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        return toDTO(v);
    }

    @Override
    public List<VehicleResponseDTO> listMyVehicles(Long userId) {
        return repo.findByOwnerId(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ------------------------------
    // HELPERS
    // ------------------------------
    private void map(Vehicle v, VehicleRequestDTO dto) {
        v.setVehicleType(dto.getVehicleType());
        v.setMake(dto.getMake());
        v.setModel(dto.getModel());
        v.setVehicleYear(dto.getVehicleYear());
        v.setLicensePlate(dto.getLicensePlate());
        v.setColor(dto.getColor());
        v.setPassengerCapacity(dto.getPassengerCapacity());
        v.setCargoCapacityKg(dto.getCargoCapacityKg());
        v.setCargoVolumeCubicMeters(dto.getCargoVolumeCubicMeters());
        v.setVehicleCondition(dto.getVehicleCondition());
        v.setInsuranceProvider(dto.getInsuranceProvider());
        v.setInsurancePolicyNumber(dto.getInsurancePolicyNumber());
        v.setInsuranceExpiryDate(dto.getInsuranceExpiryDate());
        v.setRegistrationExpiryDate(dto.getRegistrationExpiryDate());
        v.setImageUrl(dto.getImageUrl());
    }

    private VehicleResponseDTO toDTO(Vehicle v) {
        VehicleResponseDTO dto = new VehicleResponseDTO();
        dto.setId(v.getId());
        dto.setVehicleType(v.getVehicleType());
        dto.setMake(v.getMake());
        dto.setModel(v.getModel());
        dto.setVehicleYear(v.getVehicleYear());
        dto.setLicensePlate(v.getLicensePlate());
        dto.setColor(v.getColor());
        dto.setPassengerCapacity(v.getPassengerCapacity());
        dto.setCargoCapacityKg(v.getCargoCapacityKg());
        dto.setCargoVolumeCubicMeters(v.getCargoVolumeCubicMeters());
        dto.setVehicleCondition(v.getVehicleCondition());
        dto.setIsAvailable(v.getIsAvailable());
        dto.setInsuranceProvider(v.getInsuranceProvider());
        dto.setInsurancePolicyNumber(v.getInsurancePolicyNumber());
        dto.setInsuranceExpiryDate(v.getInsuranceExpiryDate());
        dto.setRegistrationExpiryDate(v.getRegistrationExpiryDate());
        dto.setCreatedAt(v.getCreatedAt());
        dto.setUpdatedAt(v.getUpdatedAt());
        dto.setImageUrl(v.getImageUrl());

        return dto;
    }
}
