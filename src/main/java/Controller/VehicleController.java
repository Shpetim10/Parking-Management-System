package Controller;

import Dto.Creation.CreateVehicleDto;
import Model.Vehicle;
import Repository.VehicleRepository;
import Service.VehicleService;

public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    public void createVehicle(String userId, String plateNumber) {
        CreateVehicleDto dto=new CreateVehicleDto(userId,plateNumber);

        Vehicle vehicle=vehicleService.createVehicle(dto);

        if(vehicle == null) throw new RuntimeException("Vehicle creation failed");
    }
}
