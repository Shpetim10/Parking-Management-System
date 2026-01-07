package Model;

import java.util.Objects;

public class Vehicle {

    private final String plateNumber;
    private final String userId;

    public Vehicle(String plateNumber, String userId) {
        this.plateNumber = Objects.requireNonNull(plateNumber);
        this.userId = Objects.requireNonNull(userId);
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getUserId() {
        return userId;
    }
}