package UnitTesting.ArtjolZaimi;

import Model.DiscountInfo;
import Repository.impl.InMemoryDiscountPolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;

// Unit Tests for M-39: InMemoryDiscountPolicyRepository.findDiscountForUser

class InMemoryDiscountPolicyRepositoryFindDiscountForUserTest {

    private InMemoryDiscountPolicyRepository repository;

    @Mock
    private DiscountInfo mockDefaultDiscount;

    @Mock
    private DiscountInfo mockUserDiscount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new InMemoryDiscountPolicyRepository(mockDefaultDiscount);
    }

    @Test
    @DisplayName("findDiscountForUser returns default discount for unknown user")
    void testFindDiscountForUser_UnknownUser() {
        DiscountInfo result = repository.findDiscountForUser("unknown-user");

        assertEquals(mockDefaultDiscount, result);
    }

    @Test
    @DisplayName("findDiscountForUser returns saved discount for known user")
    void testFindDiscountForUser_KnownUser() {
        repository.save("user-1", mockUserDiscount);

        DiscountInfo result = repository.findDiscountForUser("user-1");

        assertEquals(mockUserDiscount, result);
    }

    @Test
    @DisplayName("findDiscountForUser throws exception for null userId")
    void testFindDiscountForUser_NullUserId() {
        assertThrows(NullPointerException.class, () -> {
            repository.findDiscountForUser(null);
        });
    }

    @Test
    @DisplayName("findDiscountForUser with real discount objects")
    void testFindDiscountForUser_RealDiscountObjects() {
        DiscountInfo defaultDiscount = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        DiscountInfo userDiscount = new DiscountInfo(
                new BigDecimal("0.15"),
                new BigDecimal("0.10"),
                new BigDecimal("5.00"),
                true,
                2
        );

        InMemoryDiscountPolicyRepository repo =
                new InMemoryDiscountPolicyRepository(defaultDiscount);

        repo.save("user-premium", userDiscount);

        DiscountInfo result = repo.findDiscountForUser("user-premium");
        assertEquals(userDiscount, result);
        assertEquals(new BigDecimal("0.15"), result.getSubscriptionDiscountPercent());
    }



    @Test
    @DisplayName("findDiscountForUser returns updated discount after overwrite")
    void testFindDiscountForUser_AfterOverwrite() {
        DiscountInfo oldDiscount = mock(DiscountInfo.class);
        DiscountInfo newDiscount = mock(DiscountInfo.class);

        repository.save("user-1", oldDiscount);
        repository.save("user-1", newDiscount);

        DiscountInfo result = repository.findDiscountForUser("user-1");
        assertEquals(newDiscount, result);
        assertNotEquals(oldDiscount, result);
    }

    @Test
    @DisplayName("findDiscountForUser does not modify internal state")
    void testFindDiscountForUser_DoesNotModifyState() {
        repository.save("user-1", mockUserDiscount);

        repository.findDiscountForUser("user-1");
        repository.findDiscountForUser("user-1");
        repository.findDiscountForUser("user-1");

        assertEquals(mockUserDiscount, repository.findDiscountForUser("user-1"));
    }


    @Test
    @DisplayName("findDiscountForUser case sensitive userId")
    void testFindDiscountForUser_CaseSensitiveUserId() {
        repository.save("User-1", mockUserDiscount);

        DiscountInfo resultLower = repository.findDiscountForUser("user-1");
        DiscountInfo resultUpper = repository.findDiscountForUser("User-1");

        assertEquals(mockDefaultDiscount, resultLower);
        assertEquals(mockUserDiscount, resultUpper);
    }

    @Test
    @DisplayName("findDiscountForUser with numeric userId")
    void testFindDiscountForUser_NumericUserId() {
        repository.save("12345", mockUserDiscount);

        DiscountInfo result = repository.findDiscountForUser("12345");
        assertEquals(mockUserDiscount, result);
    }

}
