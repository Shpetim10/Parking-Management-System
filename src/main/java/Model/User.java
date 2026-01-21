package Model;

import Enum.UserStatus;
import java.util.Objects;

public class User {

    private final String id;
    private UserStatus status;

    public User(String id, UserStatus status) {
        this.id = Objects.requireNonNull(id);
        this.status = Objects.requireNonNull(status);
    }

    public String getId() {
        return id;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}