package Repository.impl;

import Model.Vehicle;
import Repository.VehicleRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryVehicleRepository implements VehicleRepository {

    private final Map<String, Vehicle> byPlate = new HashMap<>();

    @Override
    public Optional<Vehicle> findByPlate(String plateNumber) {
        return Optional.ofNullable(byPlate.get(plateNumber));
    }

    @Override
    public void save(Vehicle vehicle) {
        if(exists(vehicle.getPlateNumber())) throw new IllegalArgumentException("A vehicle with this plate number already exists!");
        byPlate.put(vehicle.getPlateNumber(), vehicle);
    }

    @Override
    public boolean exists(String plateNumber) {
        return this.byPlate.containsKey(plateNumber);
    }
}