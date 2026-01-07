package Repository;

import Enum.ZoneType;
import Model.Tariff;

public interface TariffRepository {
    Tariff findByZoneType(ZoneType zoneType);
    void save(Tariff tariff);
}
