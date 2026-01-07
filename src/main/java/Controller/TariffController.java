package Controller;

import Dto.Tariff.TariffDto;
import Model.Tariff;
import Repository.TariffRepository;
import Enum.ZoneType;

import java.util.Objects;

public class TariffController {
    private final TariffRepository tariffRepository;

    public TariffController(TariffRepository tariffRepository) {
        this.tariffRepository = Objects.requireNonNull(tariffRepository);
    }

    public TariffDto getTariffForZone(String zoneTypeName) {
        Objects.requireNonNull(zoneTypeName, "zoneTypeName must not be null");
        var zoneType = Enum.valueOf(ZoneType.class, zoneTypeName);
        Tariff tariff = tariffRepository.findByZoneType(zoneType);
        return new TariffDto(
                tariff.getZoneType(),
                tariff.getBaseHourlyRate(),
                tariff.getDailyCap(),
                tariff.isOvernightFlatRateEnabled(),
                tariff.getOvernightFlatRate(),
                tariff.getWeekendOrHolidaySurchargePercent()
        );
    }

    public void saveTariff(TariffDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");
        Tariff tariff = new Tariff(
                dto.zoneType(),
                dto.baseHourlyRate(),
                dto.dailyCap(),
                dto.overnightFlatRateEnabled(),
                dto.overnightFlatRate(),
                dto.weekendOrHolidaySurchargePercent()
        );
        tariffRepository.save(tariff);
    }
}
