package Service.impl;

import Dto.Creation.CreateVehicleDto;
import Model.Vehicle;
import Repository.ParkingZoneRepository;
import Repository.UserRepository;
import Repository.VehicleRepository;
import Service.VehicleService;

import java.util.Objects;

public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository);
        this.userRepository = userRepository;
    }

    @Override
    public Vehicle createVehicle(CreateVehicleDto vehicleDto) {
        Objects.requireNonNull(vehicleDto);

        if(!userRepository.exists(vehicleDto.getUserId())) throw new IllegalArgumentException("User with this id does not exist!");

        Vehicle vehicle = new Vehicle(vehicleDto.getPlateNumber(), vehicleDto.getUserId());

        vehicleRepository.save(vehicle);

        return vehicle;
    }
}
