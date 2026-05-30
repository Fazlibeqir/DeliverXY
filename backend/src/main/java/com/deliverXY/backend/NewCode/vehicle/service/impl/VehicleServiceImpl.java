package com.deliverXY.backend.NewCode.vehicle.service.impl;

import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.repository.AppUserRepository;
import com.deliverXY.backend.NewCode.vehicle.domain.Vehicle;
import com.deliverXY.backend.NewCode.vehicle.dto.VehicleRequestDTO;
import com.deliverXY.backend.NewCode.vehicle.dto.VehicleResponseDTO;
import com.deliverXY.backend.NewCode.vehicle.repository.VehicleRepository;
import com.deliverXY.backend.NewCode.vehicle.service.VehicleLicensePlateValidator;
import com.deliverXY.backend.NewCode.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository repo;
    private final AppUserRepository userRepo;
    private final VehicleLicensePlateValidator licensePlateValidator;

    @Override
    @Transactional
    public VehicleResponseDTO create(Long userId, VehicleRequestDTO dto) {

        licensePlateValidator.assertUnique(null, dto.getLicensePlate());

        Long ownerId = Objects.requireNonNull(userId, "userId");
        AppUser owner = userRepo.findById(ownerId)
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

        Long vehicleId = Objects.requireNonNull(id, "id");
        Vehicle v = repo.findById(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));

        if (!v.getOwner().getId().equals(userId))
            throw new BadRequestException("You do not own this vehicle");

        if (!v.getLicensePlate().equals(dto.getLicensePlate())) {
            licensePlateValidator.assertUnique(vehicleId, dto.getLicensePlate());
        }

        map(v, dto);
        repo.save(v);

        return toDTO(v);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Long vehicleId = Objects.requireNonNull(id, "id");
        Vehicle v = repo.findById(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));

        if (!v.getOwner().getId().equals(userId))
            throw new BadRequestException("You do not own this vehicle");

        repo.delete(v);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponseDTO getById(Long id) {
        Long vehicleId = Objects.requireNonNull(id, "id");
        Vehicle v = repo.findById(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        return toDTO(v);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> listMyVehicles(Long userId) {
        return repo.findByOwnerId(userId, PageRequest.of(0, 50))
                .getContent()
                .stream()
                .map(this::toDTO)
                .toList();
    }

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
