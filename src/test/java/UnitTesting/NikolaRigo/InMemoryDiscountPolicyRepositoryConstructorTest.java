package UnitTesting.NikolaRigo;

import org.junit.jupiter.api.Test;
import Model.DiscountInfo;
import Repository.impl.InMemoryDiscountPolicyRepository;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryDiscountPolicyRepositoryConstructorTest {

    @Test
    void constructor_WithValidDefaultDiscountInfo_ShouldCreateInstance() {
        // Arrange
        DiscountInfo defaultDiscount = new DiscountInfo(
                new BigDecimal("0.10"),
                new BigDecimal("0.05"),
                new BigDecimal("5.00"),
                false,
                0
        );

        // Act
        InMemoryDiscountPolicyRepository repository =
                new InMemoryDiscountPolicyRepository(defaultDiscount);

        // Assert
        assertNotNull(repository);
    }

    @Test
    void constructor_WithNullDefaultDiscountInfo_ShouldThrowNullPointerException() {
        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new InMemoryDiscountPolicyRepository(null)
        );

        assertEquals("defaultDiscountInfo must not be null", exception.getMessage());
    }

    @Test
    void constructor_ShouldStoreDefaultDiscountInfo() {
        // Arrange
        DiscountInfo defaultDiscount = new DiscountInfo(
                new BigDecimal("0.10"),
                new BigDecimal("0.05"),
                new BigDecimal("5.00"),
                false,
                0
        );

        // Act
        InMemoryDiscountPolicyRepository repository =
                new InMemoryDiscountPolicyRepository(defaultDiscount);

        // Assert
        // The default discount should be returned for any user not explicitly saved
        DiscountInfo result = repository.findDiscountForUser("any-user-id");
        assertEquals(defaultDiscount, result);
    }

    @Test
    void constructor_ShouldInitializeWithEmptyUserMap() {
        // Arrange
        DiscountInfo defaultDiscount = new DiscountInfo(
                new BigDecimal("0.10"),
                new BigDecimal("0.05"),
                new BigDecimal("5.00"),
                false,
                0
        );

        // Act
        InMemoryDiscountPolicyRepository repository =
                new InMemoryDiscountPolicyRepository(defaultDiscount);

        // Assert
        // Any user lookup should return the default discount (map is empty)
        DiscountInfo result1 = repository.findDiscountForUser("user1");
        DiscountInfo result2 = repository.findDiscountForUser("user2");
        DiscountInfo result3 = repository.findDiscountForUser("user3");

        assertEquals(defaultDiscount, result1);
        assertEquals(defaultDiscount, result2);
        assertEquals(defaultDiscount, result3);
    }

    @Test
    void constructor_WithDifferentDefaultDiscounts_ShouldCreateSeparateInstances() {
        // Arrange
        DiscountInfo defaultDiscount1 = new DiscountInfo(
                new BigDecimal("0.10"),
                new BigDecimal("0.05"),
                new BigDecimal("5.00"),
                false,
                0
        );
        DiscountInfo defaultDiscount2 = new DiscountInfo(
                new BigDecimal("0.20"),
                new BigDecimal("0.15"),
                new BigDecimal("10.00"),
                true,
                2
        );

        // Act
        InMemoryDiscountPolicyRepository repository1 =
                new InMemoryDiscountPolicyRepository(defaultDiscount1);
        InMemoryDiscountPolicyRepository repository2 =
                new InMemoryDiscountPolicyRepository(defaultDiscount2);

        // Assert
        assertNotSame(repository1, repository2);
        assertEquals(defaultDiscount1, repository1.findDiscountForUser("user"));
        assertEquals(defaultDiscount2, repository2.findDiscountForUser("user"));
    }

    @Test
    void constructor_MultipleInstances_ShouldBeIndependent() {
        // Arrange
        DiscountInfo defaultDiscount1 = new DiscountInfo(
                new BigDecimal("0.10"),
                new BigDecimal("0.05"),
                new BigDecimal("5.00"),
                false,
                0
        );
        DiscountInfo defaultDiscount2 = new DiscountInfo(
                new BigDecimal("0.20"),
                new BigDecimal("0.15"),
                new BigDecimal("10.00"),
                false,
                0
        );

        // Act
        InMemoryDiscountPolicyRepository repository1 =
                new InMemoryDiscountPolicyRepository(defaultDiscount1);
        InMemoryDiscountPolicyRepository repository2 =
                new InMemoryDiscountPolicyRepository(defaultDiscount2);

        DiscountInfo customDiscount = new DiscountInfo(
                new BigDecimal("0.25"),
                new BigDecimal("0.10"),
                new BigDecimal("15.00"),
                true,
                3
        );
        repository1.save("user1", customDiscount);

        // Assert
        // repository1 should have the custom discount for user1
        assertEquals(customDiscount, repository1.findDiscountForUser("user1"));

        // repository2 should still return its default for user1 (independent state)
        assertEquals(defaultDiscount2, repository2.findDiscountForUser("user1"));
    }

    @Test
    void constructor_ShouldAcceptSameDefaultDiscountMultipleTimes() {
        // Arrange
        DiscountInfo defaultDiscount = new DiscountInfo(
                new BigDecimal("0.10"),
                new BigDecimal("0.05"),
                new BigDecimal("5.00"),
                false,
                0
        );

        // Act
        InMemoryDiscountPolicyRepository repository1 =
                new InMemoryDiscountPolicyRepository(defaultDiscount);
        InMemoryDiscountPolicyRepository repository2 =
                new InMemoryDiscountPolicyRepository(defaultDiscount);

        // Assert
        assertNotSame(repository1, repository2);
        assertSame(defaultDiscount, repository1.findDiscountForUser("user"));
        assertSame(defaultDiscount, repository2.findDiscountForUser("user"));
    }

    @Test
    void constructor_WithZeroDiscountDefaults_ShouldCreateInstance() {
        // Arrange
        DiscountInfo defaultDiscount = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        // Act
        InMemoryDiscountPolicyRepository repository =
                new InMemoryDiscountPolicyRepository(defaultDiscount);

        // Assert
        assertNotNull(repository);
        assertEquals(defaultDiscount, repository.findDiscountForUser("user"));
    }

    @Test
    void constructor_WithFreeHoursDefault_ShouldCreateInstance() {
        // Arrange
        DiscountInfo defaultDiscount = new DiscountInfo(
                new BigDecimal("0.10"),
                new BigDecimal("0.05"),
                new BigDecimal("5.00"),
                true,
                5
        );

        // Act
        InMemoryDiscountPolicyRepository repository =
                new InMemoryDiscountPolicyRepository(defaultDiscount);

        // Assert
        assertNotNull(repository);
        assertEquals(defaultDiscount, repository.findDiscountForUser("user"));
    }

    @Test
    void constructor_WithHighDiscountPercentages_ShouldCreateInstance() {
        // Arrange
        DiscountInfo defaultDiscount = new DiscountInfo(
                new BigDecimal("0.50"),
                new BigDecimal("0.75"),
                new BigDecimal("100.00"),
                true,
                10
        );

        // Act
        InMemoryDiscountPolicyRepository repository =
                new InMemoryDiscountPolicyRepository(defaultDiscount);

        // Assert
        assertNotNull(repository);
        assertEquals(defaultDiscount, repository.findDiscountForUser("user"));
    }

    @Test
    void constructor_ShouldReturnDefaultForMultipleUnknownUsers() {
        // Arrange
        DiscountInfo defaultDiscount = new DiscountInfo(
                new BigDecimal("0.10"),
                new BigDecimal("0.05"),
                new BigDecimal("5.00"),
                false,
                0
        );

        // Act
        InMemoryDiscountPolicyRepository repository =
                new InMemoryDiscountPolicyRepository(defaultDiscount);

        // Assert
        String[] userIds = {"user1", "user2", "user3", "user4", "user5"};
        for (String userId : userIds) {
            DiscountInfo result = repository.findDiscountForUser(userId);
            assertSame(defaultDiscount, result);
        }
    }

    @Test
    void constructor_AfterSavingCustomDiscount_ShouldStillReturnDefaultForOthers() {
        // Arrange
        DiscountInfo defaultDiscount = new DiscountInfo(
                new BigDecimal("0.10"),
                new BigDecimal("0.05"),
                new BigDecimal("5.00"),
                false,
                0
        );

        // Act
        InMemoryDiscountPolicyRepository repository =
                new InMemoryDiscountPolicyRepository(defaultDiscount);

        DiscountInfo customDiscount = new DiscountInfo(
                new BigDecimal("0.25"),
                new BigDecimal("0.10"),
                new BigDecimal("15.00"),
                true,
                3
        );
        repository.save("special-user", customDiscount);

        // Assert
        assertEquals(customDiscount, repository.findDiscountForUser("special-user"));
        assertEquals(defaultDiscount, repository.findDiscountForUser("regular-user"));
        assertEquals(defaultDiscount, repository.findDiscountForUser("another-user"));
    }
}