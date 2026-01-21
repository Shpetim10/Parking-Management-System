package Controller;

import Dto.Session.StartSessionRequestDto;
import Dto.Session.StartSessionResponseDto;
import Enum.TimeOfDayBand;
import Enum.DayType;
import Model.ParkingSession;
import Model.ParkingSpot;
import Model.ParkingZone;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import Repository.UserRepository;
import Service.DurationCalculator;
import Service.EligibilityService;
import Service.ZoneAllocationService;
import Settings.Settings;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

public class ParkingSessionController {

    private final ParkingSessionRepository sessionRepo;
    private final ParkingZoneRepository zoneRepo;
    private UserRepository userRepository;
    private EligibilityService eligibilityService;
    private ZoneAllocationService zoneAllocationService;
    private DurationCalculator durationCalculator;

    public ParkingSessionController(ParkingSessionRepository sessionRepo, ParkingZoneRepository zoneRepo) {
        this.sessionRepo = Objects.requireNonNull(sessionRepo);
        this.zoneRepo = Objects.requireNonNull(zoneRepo);
    }

    public StartSessionResponseDto startSession(StartSessionRequestDto dto) {
        Objects.requireNonNull(dto);

        ParkingZone zone = zoneRepo.findById(dto.zoneId());

        ParkingSpot spot = zone.getSpots().stream()
                .filter(s -> s.getSpotId().equals(dto.spotId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Spot not found"));

        spot.occupy();

        String sessionId = UUID.randomUUID().toString();

        TimeOfDayBand time= getTimeOfDayBand(dto.startTime());
        DayType dayType= getDayType(dto.startTime(), dto.isHoliday());

        ParkingSession session = new ParkingSession(
                sessionId,
                dto.userId(),
                dto.vehiclePlate(),
                zone.getZoneId(),
                spot.getSpotId(),
                time,
                dayType,
                dto.zoneType(),
                dto.startTime()
        );

        sessionRepo.save(session);

        return new StartSessionResponseDto(
                sessionId,
                session.getState(),
                dayType,
                time,
                session.getStartTime()
        );
    }

    public boolean closeSession(String sessionId, LocalDateTime endTime) {
        Objects.requireNonNull(sessionId);
        Objects.requireNonNull(endTime);

        ParkingSession session = sessionRepo.findById(sessionId).orElse(null);
        if (session == null) return false;

        session.close(endTime);
        sessionRepo.save(session);
        return true;
    }

    public TimeOfDayBand getTimeOfDayBand(LocalDateTime startTime) {
        if (startTime == null) return null;

        LocalTime time = startTime.toLocalTime();

        if (!time.isBefore(Settings.START_PEAK_TIME)
                && time.isBefore(Settings.END_PEAK_TIME)) {
            return TimeOfDayBand.PEAK;
        }

        return TimeOfDayBand.OFF_PEAK;
    }

    public DayType getDayType(LocalDateTime startTime, boolean isHoliday) {
        if (startTime == null) return null;

        if (isHoliday) {
            return DayType.HOLIDAY;
        }

        if (startTime.getDayOfWeek() == DayOfWeek.SATURDAY || startTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return DayType.WEEKEND;
        }

        return DayType.WEEKDAY;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setEligibilityService(EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    public void setZoneAllocationService(ZoneAllocationService zoneAllocationService) {
        this.zoneAllocationService = zoneAllocationService;
    }

    public void setDurationCalculator(DurationCalculator durationCalculator) {
        this.durationCalculator = durationCalculator;
    }
}