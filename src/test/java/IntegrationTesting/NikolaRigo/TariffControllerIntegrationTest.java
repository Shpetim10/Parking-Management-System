package IntegrationTesting.NikolaRigo;

import Controller.TariffController;
import Dto.Tariff.TariffDto;
import Enum.ZoneType;
import Model.Tariff;
import Repository.TariffRepository;
import Repository.impl.InMemoryTariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Tests - TariffController")
class TariffControllerIntegrationTest {

    private TariffController tariffController;
    private TariffRepository tariffRepository;

    // Pre-configured tariffs for testing
    private Tariff standardTariff;
    private Tariff evTariff;
    private Tariff vipTariff;

    @BeforeEach
    void setUp() {
        // Initialize tariffs
        standardTariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("5.00"),      // baseHourlyRate
                new BigDecimal("50.00"),     // dailyCap
                new BigDecimal("0.20")       // weekendOrHolidaySurchargePercent (20%)
        );

        evTariff = new Tariff(
                ZoneType.EV,
                new BigDecimal("3.00"),      // baseHourlyRate
                new BigDecimal("30.00"),     // dailyCap
                new BigDecimal("0.15")       // weekendOrHolidaySurchargePercent (15%)
        );

        vipTariff = new Tariff(
                ZoneType.VIP,
                new BigDecimal("10.00"),     // baseHourlyRate
                new BigDecimal("100.00"),    // dailyCap
                new BigDecimal("0.25")       // weekendOrHolidaySurchargePercent (25%)
        );

        // Initialize repository with tariffs
        Map<ZoneType, Tariff> tariffMap = new EnumMap<>(ZoneType.class);
        tariffMap.put(ZoneType.STANDARD, standardTariff);
        tariffMap.put(ZoneType.EV, evTariff);
        tariffMap.put(ZoneType.VIP, vipTariff);

        tariffRepository = new InMemoryTariffRepository(tariffMap);

        // Create controller with real repository
        tariffController = new TariffController(tariffRepository);
    }

    // =========================================================================
    // CONSTRUCTOR TESTS
    // =========================================================================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should throw NullPointerException when repository is null")
        void constructor_WhenRepositoryNull_ShouldThrow() {
            assertThrows(NullPointerException.class, () ->
                    new TariffController(null)
            );
        }

        @Test
        @DisplayName("Should create controller with valid repository")
        void constructor_WithValidRepository_ShouldCreate() {
            TariffController controller = new TariffController(tariffRepository);
            assertNotNull(controller);
        }
    }

    // =========================================================================
    // getTariffForZone() INTEGRATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("getTariffForZone() Integration Tests")
    class GetTariffForZoneTests {

        @Test
        @DisplayName("Should return STANDARD tariff from repository")
        void getTariffForZone_Standard_ShouldReturnTariff() {
            // Act
            TariffDto result = tariffController.getTariffForZone("STANDARD");

            // Assert
            assertNotNull(result);
            assertEquals(ZoneType.STANDARD, result.zoneType());
            assertEquals(new BigDecimal("5.00"), result.baseHourlyRate());
            assertEquals(new BigDecimal("50.00"), result.dailyCap());
            assertEquals(new BigDecimal("0.20"), result.weekendOrHolidaySurchargePercent());
        }

        @Test
        @DisplayName("Should return EV tariff from repository")
        void getTariffForZone_EV_ShouldReturnTariff() {
            // Act
            TariffDto result = tariffController.getTariffForZone("EV");

            // Assert
            assertNotNull(result);
            assertEquals(ZoneType.EV, result.zoneType());
            assertEquals(new BigDecimal("3.00"), result.baseHourlyRate());
            assertEquals(new BigDecimal("30.00"), result.dailyCap());
            assertEquals(new BigDecimal("0.15"), result.weekendOrHolidaySurchargePercent());
        }

        @Test
        @DisplayName("Should return VIP tariff from repository")
        void getTariffForZone_VIP_ShouldReturnTariff() {
            // Act
            TariffDto result = tariffController.getTariffForZone("VIP");

            // Assert
            assertNotNull(result);
            assertEquals(ZoneType.VIP, result.zoneType());
            assertEquals(new BigDecimal("10.00"), result.baseHourlyRate());
            assertEquals(new BigDecimal("100.00"), result.dailyCap());
            assertEquals(new BigDecimal("0.25"), result.weekendOrHolidaySurchargePercent());
        }

        @Test
        @DisplayName("Should throw NullPointerException when zoneTypeName is null")
        void getTariffForZone_WhenNull_ShouldThrow() {
            assertThrows(NullPointerException.class, () ->
                    tariffController.getTariffForZone(null)
            );
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for invalid zone type name")
        void getTariffForZone_InvalidZoneName_ShouldThrow() {
            assertThrows(IllegalArgumentException.class, () ->
                    tariffController.getTariffForZone("INVALID_ZONE")
            );
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty zone type name")
        void getTariffForZone_EmptyZoneName_ShouldThrow() {
            assertThrows(IllegalArgumentException.class, () ->
                    tariffController.getTariffForZone("")
            );
        }

        @Test
        @DisplayName("Should be case-sensitive for zone type name")
        void getTariffForZone_LowerCase_ShouldThrow() {
            assertThrows(IllegalArgumentException.class, () ->
                    tariffController.getTariffForZone("standard")
            );
        }
    }

    // =========================================================================
    // saveTariff() INTEGRATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("saveTariff() Integration Tests")
    class SaveTariffTests {

        @Test
        @DisplayName("Should save new tariff to repository")
        void saveTariff_NewTariff_ShouldPersist() {
            // Arrange - Create a new STANDARD tariff with different values
            TariffDto newTariff = new TariffDto(
                    ZoneType.STANDARD,
                    new BigDecimal("8.00"),      // New rate
                    new BigDecimal("80.00"),     // New cap
                    new BigDecimal("0.30")       // New surcharge
            );

            // Act
            tariffController.saveTariff(newTariff);

            // Assert - Verify tariff was updated in repository
            TariffDto retrieved = tariffController.getTariffForZone("STANDARD");
            assertEquals(new BigDecimal("8.00"), retrieved.baseHourlyRate());
            assertEquals(new BigDecimal("80.00"), retrieved.dailyCap());
            assertEquals(new BigDecimal("0.30"), retrieved.weekendOrHolidaySurchargePercent());
        }

        @Test
        @DisplayName("Should update existing EV tariff in repository")
        void saveTariff_UpdateEV_ShouldPersist() {
            // Arrange
            TariffDto updatedEV = new TariffDto(
                    ZoneType.EV,
                    new BigDecimal("4.50"),
                    new BigDecimal("45.00"),
                    new BigDecimal("0.10")
            );

            // Act
            tariffController.saveTariff(updatedEV);

            // Assert
            TariffDto retrieved = tariffController.getTariffForZone("EV");
            assertEquals(new BigDecimal("4.50"), retrieved.baseHourlyRate());
            assertEquals(new BigDecimal("45.00"), retrieved.dailyCap());
            assertEquals(new BigDecimal("0.10"), retrieved.weekendOrHolidaySurchargePercent());
        }

        @Test
        @DisplayName("Should update existing VIP tariff in repository")
        void saveTariff_UpdateVIP_ShouldPersist() {
            // Arrange
            TariffDto updatedVIP = new TariffDto(
                    ZoneType.VIP,
                    new BigDecimal("15.00"),
                    new BigDecimal("150.00"),
                    new BigDecimal("0.35")
            );

            // Act
            tariffController.saveTariff(updatedVIP);

            // Assert
            TariffDto retrieved = tariffController.getTariffForZone("VIP");
            assertEquals(new BigDecimal("15.00"), retrieved.baseHourlyRate());
            assertEquals(new BigDecimal("150.00"), retrieved.dailyCap());
            assertEquals(new BigDecimal("0.35"), retrieved.weekendOrHolidaySurchargePercent());
        }

        @Test
        @DisplayName("Should throw NullPointerException when dto is null")
        void saveTariff_WhenDtoNull_ShouldThrow() {
            assertThrows(NullPointerException.class, () ->
                    tariffController.saveTariff(null)
            );
        }

        @Test
        @DisplayName("Should throw exception for negative base hourly rate")
        void saveTariff_NegativeBaseRate_ShouldThrow() {
            TariffDto invalidTariff = new TariffDto(
                    ZoneType.STANDARD,
                    new BigDecimal("-5.00"),    // Negative rate
                    new BigDecimal("50.00"),
                    new BigDecimal("0.20")
            );

            assertThrows(IllegalArgumentException.class, () ->
                    tariffController.saveTariff(invalidTariff)
            );
        }

        @Test
        @DisplayName("Should throw exception for negative daily cap")
        void saveTariff_NegativeDailyCap_ShouldThrow() {
            TariffDto invalidTariff = new TariffDto(
                    ZoneType.STANDARD,
                    new BigDecimal("5.00"),
                    new BigDecimal("-50.00"),   // Negative cap
                    new BigDecimal("0.20")
            );

            assertThrows(IllegalArgumentException.class, () ->
                    tariffController.saveTariff(invalidTariff)
            );
        }

        @Test
        @DisplayName("Should allow null daily cap")
        void saveTariff_NullDailyCap_ShouldSucceed() {
            // Arrange
            TariffDto tariffWithNullCap = new TariffDto(
                    ZoneType.STANDARD,
                    new BigDecimal("5.00"),
                    null,                       // Null cap allowed
                    new BigDecimal("0.20")
            );

            // Act & Assert - Should not throw
            assertDoesNotThrow(() -> tariffController.saveTariff(tariffWithNullCap));
        }

        @Test
        @DisplayName("Should allow zero base hourly rate")
        void saveTariff_ZeroBaseRate_ShouldSucceed() {
            // Arrange
            TariffDto freeParking = new TariffDto(
                    ZoneType.STANDARD,
                    BigDecimal.ZERO,
                    new BigDecimal("0.00"),
                    BigDecimal.ZERO
            );

            // Act
            tariffController.saveTariff(freeParking);

            // Assert
            TariffDto retrieved = tariffController.getTariffForZone("STANDARD");
            assertEquals(BigDecimal.ZERO, retrieved.baseHourlyRate());
        }
    }

    // =========================================================================
    // REPOSITORY INTEGRATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("Repository Integration Tests")
    class RepositoryIntegrationTests {

        @Test
        @DisplayName("Should maintain data consistency after multiple saves")
        void multipleSaves_ShouldMaintainConsistency() {
            // Arrange - Multiple updates to same zone
            TariffDto update1 = new TariffDto(ZoneType.STANDARD, new BigDecimal("6.00"), new BigDecimal("60.00"), new BigDecimal("0.22"));
            TariffDto update2 = new TariffDto(ZoneType.STANDARD, new BigDecimal("7.00"), new BigDecimal("70.00"), new BigDecimal("0.24"));
            TariffDto update3 = new TariffDto(ZoneType.STANDARD, new BigDecimal("8.00"), new BigDecimal("80.00"), new BigDecimal("0.26"));

            // Act
            tariffController.saveTariff(update1);
            tariffController.saveTariff(update2);
            tariffController.saveTariff(update3);

            // Assert - Should have latest values
            TariffDto result = tariffController.getTariffForZone("STANDARD");
            assertEquals(new BigDecimal("8.00"), result.baseHourlyRate());
            assertEquals(new BigDecimal("80.00"), result.dailyCap());
            assertEquals(new BigDecimal("0.26"), result.weekendOrHolidaySurchargePercent());
        }

        @Test
        @DisplayName("Should isolate tariffs between different zone types")
        void saveTariff_DifferentZones_ShouldIsolate() {
            // Arrange
            TariffDto newStandard = new TariffDto(ZoneType.STANDARD, new BigDecimal("99.00"), new BigDecimal("999.00"), new BigDecimal("0.99"));

            // Act - Update only STANDARD
            tariffController.saveTariff(newStandard);

            // Assert - EV and VIP should remain unchanged
            TariffDto evResult = tariffController.getTariffForZone("EV");
            TariffDto vipResult = tariffController.getTariffForZone("VIP");

            assertEquals(new BigDecimal("3.00"), evResult.baseHourlyRate());
            assertEquals(new BigDecimal("10.00"), vipResult.baseHourlyRate());
        }

        @Test
        @DisplayName("Should retrieve all zone types correctly")
        void getTariffForZone_AllZones_ShouldRetrieveCorrectly() {
            // Act & Assert
            TariffDto standard = tariffController.getTariffForZone("STANDARD");
            TariffDto ev = tariffController.getTariffForZone("EV");
            TariffDto vip = tariffController.getTariffForZone("VIP");

            assertAll(
                    () -> assertEquals(ZoneType.STANDARD, standard.zoneType()),
                    () -> assertEquals(ZoneType.EV, ev.zoneType()),
                    () -> assertEquals(ZoneType.VIP, vip.zoneType()),
                    () -> assertEquals(new BigDecimal("5.00"), standard.baseHourlyRate()),
                    () -> assertEquals(new BigDecimal("3.00"), ev.baseHourlyRate()),
                    () -> assertEquals(new BigDecimal("10.00"), vip.baseHourlyRate())
            );
        }

        @Test
        @DisplayName("Should save and retrieve with precision")
        void saveTariff_WithHighPrecision_ShouldMaintainPrecision() {
            // Arrange - High precision values
            TariffDto preciseTariff = new TariffDto(
                    ZoneType.STANDARD,
                    new BigDecimal("5.99"),
                    new BigDecimal("59.99"),
                    new BigDecimal("0.199")
            );

            // Act
            tariffController.saveTariff(preciseTariff);

            // Assert
            TariffDto result = tariffController.getTariffForZone("STANDARD");
            assertEquals(new BigDecimal("5.99"), result.baseHourlyRate());
            assertEquals(new BigDecimal("59.99"), result.dailyCap());
            assertEquals(new BigDecimal("0.199"), result.weekendOrHolidaySurchargePercent());
        }

        @Test
        @DisplayName("Should handle large values correctly")
        void saveTariff_LargeValues_ShouldHandleCorrectly() {
            // Arrange - Large values
            TariffDto largeTariff = new TariffDto(
                    ZoneType.VIP,
                    new BigDecimal("999999.99"),
                    new BigDecimal("9999999.99"),
                    new BigDecimal("0.99")
            );

            // Act
            tariffController.saveTariff(largeTariff);

            // Assert
            TariffDto result = tariffController.getTariffForZone("VIP");
            assertEquals(new BigDecimal("999999.99"), result.baseHourlyRate());
            assertEquals(new BigDecimal("9999999.99"), result.dailyCap());
        }
    }

    // =========================================================================
    // END-TO-END WORKFLOW TESTS
    // =========================================================================

    @Nested
    @DisplayName("End-to-End Workflow Tests")
    class EndToEndTests {

        @Test
        @DisplayName("Should complete full CRUD cycle")
        void fullCrudCycle_ShouldWork() {
            // CREATE/READ - Initial state
            TariffDto initial = tariffController.getTariffForZone("STANDARD");
            assertEquals(new BigDecimal("5.00"), initial.baseHourlyRate());

            // UPDATE
            TariffDto updated = new TariffDto(
                    ZoneType.STANDARD,
                    new BigDecimal("7.50"),
                    new BigDecimal("75.00"),
                    new BigDecimal("0.25")
            );
            tariffController.saveTariff(updated);

            // READ after update
            TariffDto afterUpdate = tariffController.getTariffForZone("STANDARD");
            assertEquals(new BigDecimal("7.50"), afterUpdate.baseHourlyRate());
            assertEquals(new BigDecimal("75.00"), afterUpdate.dailyCap());

            // UPDATE again
            TariffDto finalUpdate = new TariffDto(
                    ZoneType.STANDARD,
                    new BigDecimal("6.00"),
                    new BigDecimal("60.00"),
                    new BigDecimal("0.20")
            );
            tariffController.saveTariff(finalUpdate);

            // READ final state
            TariffDto finalState = tariffController.getTariffForZone("STANDARD");
            assertEquals(new BigDecimal("6.00"), finalState.baseHourlyRate());
        }

        @Test
        @DisplayName("Should support updating all zone types in sequence")
        void updateAllZones_ShouldWork() {
            // Update all zones
            tariffController.saveTariff(new TariffDto(ZoneType.STANDARD, new BigDecimal("11.00"), new BigDecimal("110.00"), new BigDecimal("0.11")));
            tariffController.saveTariff(new TariffDto(ZoneType.EV, new BigDecimal("22.00"), new BigDecimal("220.00"), new BigDecimal("0.22")));
            tariffController.saveTariff(new TariffDto(ZoneType.VIP, new BigDecimal("33.00"), new BigDecimal("330.00"), new BigDecimal("0.33")));

            // Verify all updated
            assertAll(
                    () -> assertEquals(new BigDecimal("11.00"), tariffController.getTariffForZone("STANDARD").baseHourlyRate()),
                    () -> assertEquals(new BigDecimal("22.00"), tariffController.getTariffForZone("EV").baseHourlyRate()),
                    () -> assertEquals(new BigDecimal("33.00"), tariffController.getTariffForZone("VIP").baseHourlyRate())
            );
        }
    }
}
