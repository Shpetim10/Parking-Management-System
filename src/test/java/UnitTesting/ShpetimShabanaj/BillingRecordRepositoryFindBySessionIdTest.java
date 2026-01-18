package UnitTesting.ShpetimShabanaj;

import Model.BillingRecord;
import Repository.BillingRecordRepository;
import Repository.impl.InMemoryBillingRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BillingRecordRepositoryFindBySessionIdTest {
    BillingRecordRepository billingRecordRepository;

    @BeforeEach
    void setUp(){
        billingRecordRepository=new InMemoryBillingRecordRepository();
    }

    //TC-01
    @Test
    @DisplayName("TC-01: Verify finding an existing record")
    void testFindingAnExcistingRecord(){
        String sessionId="S1";
        BillingRecord billingRecord=mock(BillingRecord.class);
        when(billingRecord.getSessionId()).thenReturn(sessionId);

        billingRecordRepository.save(billingRecord);

        Optional<BillingRecord> foundRecord=billingRecordRepository.findBySessionId(sessionId);

        assertTrue(foundRecord.isPresent());
        assertEquals(billingRecord,foundRecord.get());
    }

    //Tc-02
    @Test
    @DisplayName("TC-02: Verify behavior when ID does not exist")
    void testWhenIDDoesNotExist(){
        String sessionId="S1";
        BillingRecord billingRecord=mock(BillingRecord.class);
        when(billingRecord.getSessionId()).thenReturn(sessionId);
        billingRecordRepository.save(billingRecord);

        Optional<BillingRecord> foundRecord=billingRecordRepository.findBySessionId("S2");
        assertFalse(foundRecord.isPresent());
    }

    //TC-03: Verify behavior when ID is null
    @Test
    @DisplayName("TC-02: Verify behavior when ID does not exist")
    void testWhenIDIsNull(){
        String sessionId="S1";
        BillingRecord billingRecord=mock(BillingRecord.class);
        when(billingRecord.getSessionId()).thenReturn(sessionId);
        billingRecordRepository.save(billingRecord);

        Optional<BillingRecord> foundRecord=billingRecordRepository.findBySessionId(null);
        assertFalse(foundRecord.isPresent());
    }
}
