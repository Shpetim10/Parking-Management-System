package UnitTesting.ShpetimShabanaj;

import Controller.EligibilityController;
import Dto.Eligibility.EligibilityRequestDto;
import Dto.Eligibility.EligibilityResponseDto;
import Model.EligibilityResult;
import Model.SubscriptionPlan;
import Model.User;
import Model.Vehicle;
import Repository.SubscriptionPlanRepository;
import Repository.UserRepository;
import Repository.VehicleRepository;
import Service.EligibilityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EligibilityControllerCheckEligibilityTest {
    @Mock
    private EligibilityService eligibilityService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private SubscriptionPlanRepository planRepository;

    @InjectMocks
    private EligibilityController controller;

    @Mock
    private EligibilityRequestDto requestDto;

    @Mock
    private User user;

    @Mock
    private Vehicle vehicle;

    @Mock
    private SubscriptionPlan plan;

    @Mock
    private EligibilityResult eligibilityResult;

    // TC-01
    @Test
    @DisplayName("TC-01: Should return allowed response when service allows starting a session")
    void testCheckEligibility_Allows() {
        String userId = "U1";
        String plate = "AA111AA";
        int activeSessionsForVehicle = 1;
        int totalActiveSessionsForUser = 2;
        int sessionsStartedToday = 3;
        double hoursUsedToday = 4;
        boolean hasUnpaidSessions = false;
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        when(requestDto.userId()).thenReturn(userId);
        when(requestDto.vehiclePlate()).thenReturn(plate);
        when(requestDto.activeSessionsForVehicle()).thenReturn(activeSessionsForVehicle);
        when(requestDto.totalActiveSessionsForUser()).thenReturn(totalActiveSessionsForUser);
        when(requestDto.sessionsStartedToday()).thenReturn(sessionsStartedToday);
        when(requestDto.hoursUsedToday()).thenReturn(hoursUsedToday);
        when(requestDto.hasUnpaidSessions()).thenReturn(hasUnpaidSessions);
        when(requestDto.now()).thenReturn(now);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(vehicleRepository.findByPlate(plate)).thenReturn(Optional.of(vehicle));
        when(planRepository.getPlanForUser(userId)).thenReturn(Optional.of(plan));

        when(eligibilityResult.isAllowed()).thenReturn(true);
        when(eligibilityResult.getReason()).thenReturn("OK");
        when(eligibilityService.canStartSession(
                user,
                vehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                plan,
                hasUnpaidSessions,
                now
        )).thenReturn(eligibilityResult);

        EligibilityResponseDto response = controller.checkEligibility(requestDto);

        assertAll("Verify response is mapped correctly and service was called",
                () -> verify(userRepository, times(1)).findById(userId),
                () -> verify(vehicleRepository, times(1)).findByPlate(plate),
                () -> verify(planRepository, times(1)).getPlanForUser(userId),
                () -> verify(eligibilityService, times(1)).canStartSession(
                        user,
                        vehicle,
                        activeSessionsForVehicle,
                        totalActiveSessionsForUser,
                        sessionsStartedToday,
                        hoursUsedToday,
                        plan,
                        hasUnpaidSessions,
                        now
                ),
                () -> assertTrue(response.allowed(), "Response should be allowed"),
                () -> assertEquals("OK", response.reason())
        );
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should return denied response when service disallows starting a session")
    void testCheckEligibility_Denied() {
        String userId = "U1";
        String plate = "AA111AA";

        when(requestDto.userId()).thenReturn(userId);
        when(requestDto.vehiclePlate()).thenReturn(plate);
        when(requestDto.activeSessionsForVehicle()).thenReturn(0);
        when(requestDto.totalActiveSessionsForUser()).thenReturn(0);
        when(requestDto.sessionsStartedToday()).thenReturn(0);
        when(requestDto.hoursUsedToday()).thenReturn(0.0);
        when(requestDto.hasUnpaidSessions()).thenReturn(true);
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
        when(requestDto.now()).thenReturn(now);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(vehicleRepository.findByPlate(plate)).thenReturn(Optional.of(vehicle));
        when(planRepository.getPlanForUser(userId)).thenReturn(Optional.of(plan));

        when(eligibilityResult.isAllowed()).thenReturn(false);
        when(eligibilityResult.getReason()).thenReturn("Too many sessions");
        when(eligibilityService.canStartSession(
                user,
                vehicle,
                0,
                0,
                0,
                0,
                plan,
                true,
                now
        )).thenReturn(eligibilityResult);

        EligibilityResponseDto response = controller.checkEligibility(requestDto);

        assertAll("Verify response is denied with correct reason",
                () -> assertFalse(response.allowed()),
                () -> assertEquals("Too many sessions", response.reason())
        );
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should throw when user is not found")
    void testCheckEligibility_UserNotFound() {
        String userId = "user-1";
        when(requestDto.userId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> controller.checkEligibility(requestDto));

        verify(userRepository, times(1)).findById(userId);
        verifyNoInteractions(vehicleRepository, planRepository, eligibilityService);
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should throw when vehicle is not found")
    void testCheckEligibility_VehicleNotFound() {
        String userId = "user-1";
        String plate = "AA111AA";
        when(requestDto.userId()).thenReturn(userId);
        when(requestDto.vehiclePlate()).thenReturn(plate);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(vehicleRepository.findByPlate(plate)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> controller.checkEligibility(requestDto));

        verify(userRepository, times(1)).findById(userId);
        verify(vehicleRepository, times(1)).findByPlate(plate);
        verifyNoInteractions(planRepository, eligibilityService);
    }

    // TC-05
    @Test
    @DisplayName("TC-05: Should throw when subscription plan is not found")
    void testCheckEligibility_PlanNotFound() {
        String userId = "user-1";
        String plate = "AA111AA";
        when(requestDto.userId()).thenReturn(userId);
        when(requestDto.vehiclePlate()).thenReturn(plate);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(vehicleRepository.findByPlate(plate)).thenReturn(Optional.of(vehicle));
        when(planRepository.getPlanForUser(userId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> controller.checkEligibility(requestDto));

        verify(userRepository, times(1)).findById(userId);
        verify(vehicleRepository, times(1)).findByPlate(plate);
        verify(planRepository, times(1)).getPlanForUser(userId);
        verifyNoInteractions(eligibilityService);
    }

    // TC-06
    @Test
    @DisplayName("TC-06: Should throw NullPointerException when dto is null")
    void testCheckEligibility_DtoNull() {
        assertThrows(NullPointerException.class,
                () -> controller.checkEligibility(null));

        verifyNoInteractions(userRepository, vehicleRepository, planRepository, eligibilityService);
    }

    // TC-07
    @Test
    @DisplayName("TC-07: Should throw NullPointerException when service returns null result")
    void testCheckEligibility_ServiceReturnsNull() {
        String userId = "user-1";
        String plate = "AA111AA";
        when(requestDto.userId()).thenReturn(userId);
        when(requestDto.vehiclePlate()).thenReturn(plate);
        when(requestDto.activeSessionsForVehicle()).thenReturn(0);
        when(requestDto.totalActiveSessionsForUser()).thenReturn(0);
        when(requestDto.sessionsStartedToday()).thenReturn(0);
        when(requestDto.hoursUsedToday()).thenReturn(0.0);
        when(requestDto.hasUnpaidSessions()).thenReturn(false);
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 9, 0);
        when(requestDto.now()).thenReturn(now);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(vehicleRepository.findByPlate(plate)).thenReturn(Optional.of(vehicle));
        when(planRepository.getPlanForUser(userId)).thenReturn(Optional.of(plan));

        when(eligibilityService.canStartSession(
                any(), any(), anyInt(), anyInt(), anyInt(), anyDouble(), any(), anyBoolean(), any()
        )).thenReturn(null);

        assertThrows(NullPointerException.class,
                () -> controller.checkEligibility(requestDto));
    }

    // TC-08
    @Test
    @DisplayName("TC-08: Should allow null reason from service and propagate it to response")
    void testCheckEligibility_AllowsNullReason() {
        String userId = "user-1";
        String plate = "AA111AA";

        when(requestDto.userId()).thenReturn(userId);
        when(requestDto.vehiclePlate()).thenReturn(plate);
        when(requestDto.activeSessionsForVehicle()).thenReturn(0);
        when(requestDto.totalActiveSessionsForUser()).thenReturn(0);
        when(requestDto.sessionsStartedToday()).thenReturn(0);
        when(requestDto.hoursUsedToday()).thenReturn(0.0);
        when(requestDto.hasUnpaidSessions()).thenReturn(false);
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 14, 0);
        when(requestDto.now()).thenReturn(now);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(vehicleRepository.findByPlate(plate)).thenReturn(Optional.of(vehicle));
        when(planRepository.getPlanForUser(userId)).thenReturn(Optional.of(plan));

        when(eligibilityResult.isAllowed()).thenReturn(true);
        when(eligibilityResult.getReason()).thenReturn(null);
        when(eligibilityService.canStartSession(
                any(), any(), anyInt(), anyInt(), anyInt(), anyDouble(), any(), anyBoolean(), any()
        )).thenReturn(eligibilityResult);

        EligibilityResponseDto response = controller.checkEligibility(requestDto);

        assertTrue(response.allowed());
        assertNull(response.reason());
    }
}
