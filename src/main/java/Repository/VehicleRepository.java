package Repository;

import Model.Vehicle;

import java.util.Optional;

public interface VehicleRepository {

    Optional<Vehicle> findByPlate(String plateNumber);

    void save(Vehicle vehicle);
}