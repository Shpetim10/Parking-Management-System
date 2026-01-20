package Artjol.UnitTesting;

import Model.BillingRecord;
import Model.BillingResult;
import Enum.ZoneType;
import Repository.BillingRecordRepository;
import Repository.impl.InMemoryBillingRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Optional;

// Unit Tests for M-36: BillingRecordRepository.save

class BillingRecordRepositorySaveTest {

    private BillingRecordRepository repository;

    @Mock
    private BillingRecord mockRecord;

    @Mock
    private BillingResult mockResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new InMemoryBillingRecordRepository();
    }

    @Test
    @DisplayName("save stores billing record successfully")
    void testSave_StoresBillingRecord() {
        when(mockRecord.getSessionId()).thenReturn("session-1");

        repository.save(mockRecord);

        Optional<BillingRecord> found = repository.findBySessionId("session-1");
        assertTrue(found.isPresent());
        assertEquals(mockRecord, found.get());
        verify(mockRecord, atLeastOnce()).getSessionId();
    }

    @Test
    @DisplayName("save with real billing record")
    void testSave_RealBillingRecord() {
        BillingRecord record = new BillingRecord(
                "session-1",
                "user-1",
                ZoneType.STANDARD,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                mockResult
        );

        repository.save(record);

        Optional<BillingRecord> found = repository.findBySessionId("session-1");
        assertTrue(found.isPresent());
        assertEquals("session-1", found.get().getSessionId());
        assertEquals("user-1", found.get().getUserId());
    }

    @Test
    @DisplayName("save overwrites existing record with same sessionId")
    void testSave_OverwritesExistingRecord() {
        BillingRecord record1 = new BillingRecord(
                "session-1",
                "user-1",
                ZoneType.STANDARD,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                mockResult
        );

        BillingRecord record2 = new BillingRecord(
                "session-1",
                "user-2",
                ZoneType.VIP,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(3),
                mockResult
        );

        repository.save(record1);
        repository.save(record2);

        Optional<BillingRecord> found = repository.findBySessionId("session-1");
        assertTrue(found.isPresent());
        assertEquals("user-2", found.get().getUserId());
        assertEquals(ZoneType.VIP, found.get().getZoneType());
    }

    @Test
    @DisplayName("save multiple records with different sessionIds")
    void testSave_MultipleRecords() {
        BillingRecord record1 = new BillingRecord(
                "session-1",
                "user-1",
                ZoneType.STANDARD,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                mockResult
        );

        BillingRecord record2 = new BillingRecord(
                "session-2",
                "user-2",
                ZoneType.VIP,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                mockResult
        );

        repository.save(record1);
        repository.save(record2);

        assertTrue(repository.findBySessionId("session-1").isPresent());
        assertTrue(repository.findBySessionId("session-2").isPresent());
    }

    @Test
    @DisplayName("save does not throw exception for valid record")
    void testSave_NoException() {
        BillingRecord record = new BillingRecord(
                "session-1",
                "user-1",
                ZoneType.STANDARD,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                mockResult
        );

        assertDoesNotThrow(() -> repository.save(record));
    }



    //just the test for one enum
    @Test
    @DisplayName("save with EV zone type")
    void testSave_EVZoneType() {
        BillingRecord record = new BillingRecord(
                "session-ev",
                "user-ev",
                ZoneType.EV,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                mockResult
        );

        repository.save(record);

        Optional<BillingRecord> found = repository.findBySessionId("session-ev");
        assertTrue(found.isPresent());
        assertEquals(ZoneType.EV, found.get().getZoneType());
    }

    @Test
    @DisplayName("save preserves record identity")
    void testSave_PreservesIdentity() {
        BillingRecord record = new BillingRecord(
                "session-1",
                "user-1",
                ZoneType.STANDARD,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                mockResult
        );

        repository.save(record);

        Optional<BillingRecord> found = repository.findBySessionId("session-1");
        assertTrue(found.isPresent());
        assertSame(record, found.get());
    }

}
