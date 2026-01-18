package UnitTesting.ShpetimShabanaj;

import Enum.ZoneType;
import Model.BillingRecord;
import Model.BillingResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BillingRecordConstructorTest {
    //TC-BILLREC-001
    @Test
    void testConstructorWithAllValuesCorrect(){
        String sessionId="A1";
        String userId="U1";
        ZoneType zoneType= ZoneType.VIP;
        LocalDateTime entryTime=LocalDateTime.now();
        LocalDateTime exitTime=LocalDateTime.now().plusHours(2);
        BillingResult billingResult=new BillingResult(
                new BigDecimal("100.0"),
                new BigDecimal("10.0"),
                new BigDecimal(0),
                new BigDecimal("110.0"),
                new BigDecimal("22.0"),
                new BigDecimal("132.0")
        );

        //Create an obj with the testing constructor
        BillingRecord billingRecord = new BillingRecord(
                sessionId,
                userId,
                zoneType,
                entryTime,
                exitTime,
                billingResult
        );

        assertNotNull(billingRecord);
        assertEquals(sessionId, billingRecord.getSessionId());
        assertEquals(userId, billingRecord.getUserId());
        assertEquals(zoneType, billingRecord.getZoneType());
        assertEquals(entryTime, billingRecord.getEntryTime());
        assertEquals(exitTime, billingRecord.getExitTime());
        assertEquals(billingResult, billingRecord.getBillingResult());
    }

    //TC-BILLREC-002
    @Test
    void testConstructorWithNullSessionId(){
        String userId="U1";
        ZoneType zoneType= ZoneType.VIP;
        LocalDateTime entryTime=LocalDateTime.now();
        LocalDateTime exitTime=LocalDateTime.now().plusHours(2);
        BillingResult billingResult=new BillingResult(
                new BigDecimal("100.0"),
                new BigDecimal("10.0"),
                new BigDecimal(0),
                new BigDecimal("110.0"),
                new BigDecimal("22.0"),
                new BigDecimal("132.0")
        );

        //Create an obj with the testing constructor
        NullPointerException ex =assertThrows(NullPointerException.class, ()->{
            new BillingRecord(
                    null,
                    userId,
                    zoneType,
                    entryTime,
                    exitTime,
                    billingResult
            );
        });
        assertEquals("sessionId must not be null", ex.getMessage());
    }
    //TC-BILLREC-003
    @Test
    void testConstructorWithNullUserId(){
        String sessionId="S1";
        ZoneType zoneType= ZoneType.VIP;
        LocalDateTime entryTime=LocalDateTime.now();
        LocalDateTime exitTime=LocalDateTime.now().plusHours(2);
        BillingResult billingResult=new BillingResult(
                new BigDecimal("100.0"),
                new BigDecimal("10.0"),
                new BigDecimal(0),
                new BigDecimal("110.0"),
                new BigDecimal("22.0"),
                new BigDecimal("132.0")
        );

        //Create an obj with the testing constructor
        NullPointerException ex =assertThrows(NullPointerException.class, ()->{
            new BillingRecord(
                    sessionId,
                    null,
                    zoneType,
                    entryTime,
                    exitTime,
                    billingResult
            );
        });
        assertEquals("userId must not be null", ex.getMessage());
    }
    //TC-BILLREC-004
    @Test
    void testConstructorWithNullZoneType(){
        String sessionId="S1";
        String userId="U1";
        LocalDateTime entryTime=LocalDateTime.now();
        LocalDateTime exitTime=LocalDateTime.now().plusHours(2);
        BillingResult billingResult=new BillingResult(
                new BigDecimal("100.0"),
                new BigDecimal("10.0"),
                new BigDecimal(0),
                new BigDecimal("110.0"),
                new BigDecimal("22.0"),
                new BigDecimal("132.0")
        );

        //Create an obj with the testing constructor
        NullPointerException ex =assertThrows(NullPointerException.class, ()->{
            new BillingRecord(
                    sessionId,
                    userId,
                    null,
                    entryTime,
                    exitTime,
                    billingResult
            );
        });
        assertEquals("zoneType must not be null", ex.getMessage());
    }
    //TC-BILLREC-005
    @Test
    void testConstructorWithNullEntryTime(){
        String sessionId="S1";
        String userId="U1";
        ZoneType zoneType= ZoneType.VIP;
        LocalDateTime exitTime=LocalDateTime.now().plusHours(2);
        BillingResult billingResult=new BillingResult(
                new BigDecimal("100.0"),
                new BigDecimal("10.0"),
                new BigDecimal(0),
                new BigDecimal("110.0"),
                new BigDecimal("22.0"),
                new BigDecimal("132.0")
        );

        //Create an obj with the testing constructor
        NullPointerException ex =assertThrows(NullPointerException.class, ()->{
            new BillingRecord(
                    sessionId,
                    userId,
                    zoneType,
                    null,
                    exitTime,
                    billingResult
            );
        });
        assertEquals("entryTime must not be null", ex.getMessage());
    }
    //TC-BILLREC-006
    @Test
    void testConstructorWithNullExitTime(){
        String sessionId="S1";
        String userId="U1";
        ZoneType zoneType= ZoneType.VIP;
        LocalDateTime entryTime=LocalDateTime.now();
        BillingResult billingResult=new BillingResult(
                new BigDecimal("100.0"),
                new BigDecimal("10.0"),
                new BigDecimal(0),
                new BigDecimal("110.0"),
                new BigDecimal("22.0"),
                new BigDecimal("132.0")
        );

        //Create an obj with the testing constructor
        NullPointerException ex =assertThrows(NullPointerException.class, ()->{
            new BillingRecord(
                    sessionId,
                    userId,
                    zoneType,
                    entryTime,
                    null,
                    billingResult
            );
        });
        assertEquals("exitTime must not be null", ex.getMessage());
    }
    //TC-BILLREC-007
    @Test
    void testConstructorWithNullBillingResult(){
        String sessionId="S1";
        String userId="U1";
        ZoneType zoneType= ZoneType.VIP;
        LocalDateTime entryTime=LocalDateTime.now();
        LocalDateTime exitTime=LocalDateTime.now().plusHours(2);

        //Create an obj with the testing constructor
        NullPointerException ex =assertThrows(NullPointerException.class, ()->{
            new BillingRecord(
                    sessionId,
                    userId,
                    zoneType,
                    entryTime,
                    exitTime,
                    null
            );
        });
        assertEquals("billingResult must not be null", ex.getMessage());
    }

    // TC-BILLREC-08
    @Test
    void testConstructorWithExitTimeBeforeEntryTime(){
        String sessionId="S1";
        String userId="U1";
        ZoneType zoneType= ZoneType.VIP;
        LocalDateTime entryTime=LocalDateTime.now();
        LocalDateTime exitTime=LocalDateTime.now().minusHours(2);
        BillingResult billingResult=new BillingResult(
                new BigDecimal("100.0"),
                new BigDecimal("10.0"),
                new BigDecimal(0),
                new BigDecimal("110.0"),
                new BigDecimal("22.0"),
                new BigDecimal("132.0")
        );
        //Create an obj with the testing constructor
        IllegalArgumentException ex =assertThrows(IllegalArgumentException.class, ()->{
            new BillingRecord(
                    sessionId,
                    userId,
                    zoneType,
                    entryTime,
                    exitTime,
                    billingResult
            );
        });
        assertEquals("exitTime must not be before entryTime", ex.getMessage());
    }
}
