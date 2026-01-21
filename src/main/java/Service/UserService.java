package Service;

import Dto.Creation.CreateUserDto;
import Model.User;

public interface UserService {
    User createUser(CreateUserDto userDto);
    void updateUserStatus(String userId, String status);
}
