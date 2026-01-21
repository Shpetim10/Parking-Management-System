package UnitTesting.ArtjolZaimi;

import Model.User;
import Enum.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

// Unit Tests for M-33: User.constructor

class UserConstructorTest {

    @Test
    @DisplayName("Constructor with valid parameters")
    void testConstructor_ValidParameters() {
        User user = new User("user-1", UserStatus.ACTIVE);

        assertEquals("user-1", user.getId());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("Constructor throws exception for null id")
    void testConstructor_NullId() {
        assertThrows(NullPointerException.class, () -> {
            new User(null, UserStatus.ACTIVE);
        });
    }

    @Test
    @DisplayName("Constructor throws exception for null status")
    void testConstructor_NullStatus() {
        assertThrows(NullPointerException.class, () -> {
            new User("user-1", null);
        });
    }
    //I left just one enum test
    @Test
    @DisplayName("Constructor with ACTIVE status")
    void testConstructor_ActiveStatus() {
        User user = new User("user-active", UserStatus.ACTIVE);

        assertEquals("user-active", user.getId());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }


    @Test
    @DisplayName("Constructor creates distinct instances")
    void testConstructor_DistinctInstances() {
        User user1 = new User("user-1", UserStatus.ACTIVE);
        User user2 = new User("user-1", UserStatus.ACTIVE);

        assertNotSame(user1, user2);
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getStatus(), user2.getStatus());
    }


    @Test
    @DisplayName("Constructor preserves exact id value")
    void testConstructor_PreservesIdValue() {
        String originalId = "USER-123-ABC";
        User user = new User(originalId, UserStatus.BLACKLISTED);

        assertSame(originalId, user.getId());
    }



}
