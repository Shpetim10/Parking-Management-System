package UnitTesting.ShpetimShabanaj;

import Controller.DiscountInfoController;
import Dto.DiscountInfo.DiscountInfoDto;
import Model.DiscountInfo;
import Repository.DiscountPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DiscountInfoControllerGetDiscountForUserTest {
    private DiscountPolicyRepository repository;
    private DiscountInfoController controller;

    @BeforeEach
    void setUp() {
        repository = mock(DiscountPolicyRepository.class);
        controller = new DiscountInfoController(repository);
    }

    @Test
    @DisplayName("TC-01: Should return mapped DiscountInfoDto when repository returns valid info")
    void testGetDiscountForUserHappyPath() {
        String userId = "U1";

        DiscountInfo discountInfoMock= mock(DiscountInfo.class);
        when(discountInfoMock.getSubscriptionDiscountPercent()).thenReturn(new BigDecimal("0.20"));
        when(discountInfoMock.getPromoDiscountPercent()).thenReturn(new BigDecimal("0.00"));
        when(discountInfoMock.getFreeHoursPerDay()).thenReturn(2);
        when(discountInfoMock.getPromoDiscountFixed()).thenReturn(new BigDecimal("0.00"));
        when(discountInfoMock.isSubscriptionHasFreeHours()).thenReturn(true);

        when(repository.findDiscountForUser(userId)).thenReturn(discountInfoMock);

        DiscountInfoDto result = controller.getDiscountForUser(userId);

        // Assert
        assertNotNull(result);
        assertAll("Verify all fields are mapped correctly",
                () -> assertEquals(new BigDecimal("0.20"), result.subscriptionDiscountPercent()),
                () -> assertEquals(new BigDecimal("0.00"), result.promoDiscountPercent()),
                () -> assertEquals(new BigDecimal("0.00"), result.promoDiscountFixed()),
                () -> assertTrue(result.subscriptionHasFreeHours()),
                () -> assertEquals(2, result.freeHoursPerDay())
        );
    }

    @Test
    @DisplayName("TC-02: Should throw NullPointerException when userId is null")
    void testGetDiscountWithNullUserId() {
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                controller.getDiscountForUser(null)
        );
        assertEquals("userId must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("TC-03: Should throw NullPointerException when repository returns null")
    void testGetDiscountWhenRepoReturnsNull() {
        String userId = "unknown-user";
        when(repository.findDiscountForUser(userId)).thenReturn(null);

        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                controller.getDiscountForUser(userId)
        );
        assertEquals("discountInfo must not be null", ex.getMessage());
    }
}
