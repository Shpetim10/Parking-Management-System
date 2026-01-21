package Service;

import Dto.Creation.CreateVehicleDto;
import Model.Vehicle;

public interface VehicleService {
    Vehicle createVehicle(CreateVehicleDto vehicleDto);
}
