package Controller;

import Dto.Zone.ParkingSpotDto;
import Dto.Zone.ParkingZoneDto;
import Model.ParkingSpot;
import Model.ParkingZone;
import Repository.ParkingZoneRepository;
import Enum.ZoneType;
public class ParkingZoneController {
    private final ParkingZoneRepository parkingZoneRepository;

    public ParkingZoneController(ParkingZoneRepository parkingZoneRepository) {
        this.parkingZoneRepository = parkingZoneRepository;
    }

    public void createParkingZone(ParkingZoneDto parkingZoneDto) {
        if (parkingZoneRepository.zoneExists(parkingZoneDto.getZoneId())) {
            throw new IllegalArgumentException("A Parking Zone with this id already exists");
        }
        parkingZoneRepository.save(new ParkingZone(parkingZoneDto.getZoneId(),parkingZoneDto.getZoneType(),parkingZoneDto.getMaxOccupancyThreshold()));
    }

    public void addSpot(ParkingSpotDto parkingZoneDto) {
        if(parkingZoneRepository.spotExists(parkingZoneDto.getSpotID())){
            throw new IllegalArgumentException("A Parking Spot with this id already exists");
        }

        ParkingZone zone=parkingZoneRepository.findZoneById(parkingZoneDto.getZoneId());

        ParkingSpot spot=new ParkingSpot(
                parkingZoneDto.getSpotID(),
                zone
        );

        zone.addSpot(spot);
    }
}
