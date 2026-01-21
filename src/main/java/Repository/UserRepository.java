package Repository;

import Model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String userId);

    void save(User user);

    boolean exists(String userId);
}