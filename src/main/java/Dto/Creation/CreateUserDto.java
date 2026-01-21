package Dto.Creation;

public class CreateUserDto {
    private String userId;

    public CreateUserDto(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
