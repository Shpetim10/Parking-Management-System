package UnitTesting.ShpetimShabanaj;

import Model.DiscountInfo;
import Repository.DiscountPolicyRepository;
import Repository.impl.InMemoryDiscountPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class DiscountPolicyRepositorySaveTest {
    DiscountPolicyRepository discountPolicyRepository;
    DiscountInfo defaultDiscountInfo;
    @BeforeEach
    void setUp(){
        defaultDiscountInfo=mock(DiscountInfo.class);
        discountPolicyRepository= new InMemoryDiscountPolicyRepository(defaultDiscountInfo);
    }

    //TC-01
    @Test
    @DisplayName("TC-01: Verify successful save of new record")
    void testWhenSaveOfNewRecord(){
        String userId="U1";
        DiscountInfo discountInfo=mock(DiscountInfo.class);

        discountPolicyRepository.save(userId,discountInfo);

        DiscountInfo found=discountPolicyRepository.findDiscountForUser(userId);
        assertEquals(discountInfo,found);
    }

    //TC-02: Verify overwriting existing record
    @Test
    @DisplayName("TC-02: Verify overwriting excisting record")
    void testWhenOverwritingExistingRecord(){
        String userId="U1";
        DiscountInfo mock1=mock(DiscountInfo.class);
        discountPolicyRepository.save(userId,mock1);
        DiscountInfo mock2=mock(DiscountInfo.class);
        discountPolicyRepository.save(userId,mock2);

        assertEquals(mock2,discountPolicyRepository.findDiscountForUser(userId));
    }

    //TC-03: Prevent saving with id null
    @Test
    @DisplayName("TC-03: Prevent saving with id null")
    void preventSavingWithIdNull(){
        assertThrows(NullPointerException.class,
                ()-> discountPolicyRepository.save(null, mock(DiscountInfo.class)));
    }
    //TC-04: Prevent saving with discount null
    @Test
    @DisplayName("TC-04: Prevent saving with discount null")
    void preventSavingWithDiscountNull(){
        assertThrows(NullPointerException.class,
                ()-> discountPolicyRepository.save("U1", null));
    }
}
