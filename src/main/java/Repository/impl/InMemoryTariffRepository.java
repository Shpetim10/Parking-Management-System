package Repository.impl;

import Model.Tariff;
import Repository.TariffRepository;
import Enum.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class InMemoryTariffRepository implements TariffRepository {
    private final Map<ZoneType, Tariff> tariffs = new EnumMap<>(ZoneType.class);

    public InMemoryTariffRepository(Map<ZoneType, Tariff> initialTariffs) {
        Objects.requireNonNull(initialTariffs, "initialTariffs must not be null");
        this.tariffs.putAll(initialTariffs);
    }

    @Override
    public Tariff findByZoneType(ZoneType zoneType) {
        Objects.requireNonNull(zoneType, "zoneType must not be null");
        Tariff tariff = tariffs.get(zoneType);
        if (tariff == null) {
            throw new IllegalArgumentException("No tariff configured for zoneType: " + zoneType);
        }
        return tariff;
    }

    @Override
    public void save(Tariff tariff) {
        Objects.requireNonNull(tariff, "tariff must not be null");
        tariffs.put(tariff.getZoneType(), tariff);
    }
}
