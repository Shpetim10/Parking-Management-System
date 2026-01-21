package Controller;

import Dto.Creation.CreateUserDto;
import Model.User;
import Service.UserService;

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void createrUser(String id){
        CreateUserDto dto=new CreateUserDto(id);

        User user=userService.createUser(dto);

        if(user == null) throw new RuntimeException("User creation failed");
    }

    public void updateUser(String id,String status){
        userService.updateUserStatus(id,status);
    }
}
