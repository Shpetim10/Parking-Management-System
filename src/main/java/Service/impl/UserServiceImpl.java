package Service.impl;

import Dto.Creation.CreateUserDto;
import Model.DiscountInfo;
import Model.SubscriptionPlan;
import Model.User;
import Repository.SubscriptionPlanRepository;
import Repository.UserRepository;
import Service.UserService;
import Enum.UserStatus;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;

    public UserServiceImpl(UserRepository userRepository, SubscriptionPlanRepository planRepository) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
    }

    @Override
    public User createUser(CreateUserDto userDto) {
        Objects.requireNonNull(userDto);
        Objects.requireNonNull(userDto.getUserId());

        if(userRepository.exists(userDto.getUserId())) throw new IllegalArgumentException("User with this id already exists!");

        User user = new User(userDto.getUserId(), UserStatus.ACTIVE);

        Optional<SubscriptionPlan> existing = planRepository.getPlanForUser(user.getId());
        if (existing.isEmpty()) {
            planRepository.save(user.getId(), defaultPlan());
        }


        userRepository.save(user);

        return user;
    }

    @Override
    public void updateUserStatus(String userId, String status) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(status);

        UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());


        Optional<User> opt=userRepository.findById(userId);

        if(!opt.isPresent()) throw new RuntimeException("User not found!");

        User user= opt.get();
        user.setStatus(userStatus);
        userRepository.save(user);
    }

    private static SubscriptionPlan defaultPlan() {
        return new SubscriptionPlan(1, 1, 5, 8, false, false, false,
                new DiscountInfo(new BigDecimal("0.1"), BigDecimal.ZERO, BigDecimal.ZERO, false, 0));
    }
}
