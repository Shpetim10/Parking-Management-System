package UnitTesting.ShpetimShabanaj;

import Model.ParkingSession;
import Model.ParkingZone;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import Service.impl.ZoneOccupancyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZoneOccupancyServiceTest {
    private ParkingZoneRepository zoneRepo;
    private ParkingSessionRepository sessionRepo;
    private ZoneOccupancyServiceImpl occupancyService;

    @BeforeEach
    void setUp() {
        zoneRepo = mock(ParkingZoneRepository.class);
        sessionRepo = mock(ParkingSessionRepository.class);
        occupancyService = new ZoneOccupancyServiceImpl(zoneRepo, sessionRepo);
    }

    @Test
    @DisplayName("TC-01: Should calculate partial occupancy (2/10 = 0.2)")
    void testCalculatePartialOccupancy() {
        String zoneId = "Z1";
        setupMockZone(zoneId, 10);

        List<ParkingSession> sessions = Arrays.asList(
                createMockSession(zoneId, true),  // Active
                createMockSession(zoneId, true),  // Active
                createMockSession(zoneId, false)  // Inactive
        );
        when(sessionRepo.findAll()).thenReturn(sessions);

        double ratio = occupancyService.calculateOccupancyRatioForZone(zoneId);

        assertEquals(0.2, ratio, 0.001, "Ratio for 2 active out of 10 should be 0.2");
    }

    @Test
    @DisplayName("TC-02: Should return 1.0 when all spots are occupied")
    void testCalculateFullOccupancy() {
        String zoneId = "Z2";
        setupMockZone(zoneId, 3);

        List<ParkingSession> sessions = Arrays.asList(
                createMockSession(zoneId, true),
                createMockSession(zoneId, true),
                createMockSession(zoneId, true)
        );
        when(sessionRepo.findAll()).thenReturn(sessions);

        double ratio = occupancyService.calculateOccupancyRatioForZone(zoneId);

        assertEquals(1.0, ratio, "Ratio for 3/3 spots should be 1.0");
    }

    @Test
    @DisplayName("TC-03: Should return 0.0 when no active sessions exist for the zone")
    void testCalculateEmptyOccupancy() {
        String zoneId = "Z3";
        setupMockZone(zoneId, 20);

        List<ParkingSession> sessions = Arrays.asList(
                createMockSession(zoneId, false),    // Inactive for this zone
                createMockSession("Z10", true)     // Active but for another zone
        );
        when(sessionRepo.findAll()).thenReturn(sessions);

        double ratio = occupancyService.calculateOccupancyRatioForZone(zoneId);

        assertEquals(0.0, ratio, "Ratio should be 0.0 when no matching active sessions exist.");
    }

    @Test
    @DisplayName("TC-04: Should return 0.0 for a non-existent zone ID")
    void testNonExistentZone() {
        when(zoneRepo.findById("INVALID")).thenReturn(null);

        double ratio = occupancyService.calculateOccupancyRatioForZone("INVALID");

        assertEquals(0.0, ratio, "Should return 0.0 if findById returns null.");
    }

    @Test
    @DisplayName("TC-05: Should return 0.0 for a zone with zero capacity (prevent division by zero)")
    void testZeroCapacityHandling() {
        String zoneId = "ZERO_CAPACITY";
        setupMockZone(zoneId, 0);
        when(sessionRepo.findAll()).thenReturn(Collections.emptyList());

        double ratio = occupancyService.calculateOccupancyRatioForZone(zoneId);

        assertEquals(0.0, ratio, "Should return 0.0 for zero-spot zones to avoid NaN.");
    }

    // Helper Methods
    private void setupMockZone(String zoneId, int totalSpots) {
        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getTotalSpots()).thenReturn(totalSpots);
        when(zoneRepo.findById(zoneId)).thenReturn(mockZone);
    }

    private ParkingSession createMockSession(String zoneId, boolean isActive) {
        ParkingSession session = mock(ParkingSession.class);
        when(session.getZoneId()).thenReturn(zoneId);
        when(session.isActive()).thenReturn(isActive);
        return session;
    }
}
