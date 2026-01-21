package Dto.Creation;

public class CreateVehicleDto {
    String userId;
    String plateNumber;

    public CreateVehicleDto(String userId, String plateNumber) {
        this.userId = userId;
        this.plateNumber = plateNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }
}
