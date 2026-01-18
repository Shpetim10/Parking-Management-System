package UnitTesting.ShpetimShabanaj;

import Model.DiscountInfo;
import Model.SubscriptionPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class SubscriptionPlanConstructorTest {
    @Test
    @DisplayName("TC-01: Verify that all variables initialize successfully")
    void testWhenAllVariablesInitialized(){
        DiscountInfo discountInfo=mock(DiscountInfo.class);

        SubscriptionPlan plan=new SubscriptionPlan(
                1,1,1,10.0,true,false,false,discountInfo
        );

        assertEquals(1,plan.maxConcurrentSessions);
        assertEquals(1,plan.maxConcurrentSessionsPerVehicle);
        assertEquals(1,plan.maxDailySessions);
        assertEquals(10.0, plan.maxDailyHours);
        assertTrue(plan.weekdayOnly);
        assertFalse(plan.hasEvRights);
        assertFalse(plan.hasVipRights);
        assertNotNull(plan.discountInfo);
    }

    @Test
    @DisplayName("TC-02: Verify it fails when discountInfo is null")
    void testWhenDiscountInfoIsNull(){
        assertThrows(NullPointerException.class,() -> new SubscriptionPlan(
                1,1,1,10.0,true,false,false,null
        ));
    }
}
