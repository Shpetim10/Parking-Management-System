package Controller;

import Dto.Penalty.ApplyPenaltyRequestDto;
import Dto.Penalty.ApplyPenaltyResponseDto;
import Dto.Penalty.PenaltyCalculationRequestDto;
import Dto.Penalty.PenaltyCalculationResponseDto;
import Enum.BlacklistStatus;
import Model.Penalty;
import Model.PenaltyHistory;
import Repository.PenaltyHistoryRepository;
import Service.MonitoringService;
import Service.PenaltyService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class PenaltyController {

    private static final int MAX_PENALTIES_ALLOWED = 3;
    private static final Duration BLACKLIST_WINDOW = Duration.ofDays(30);

    private final PenaltyService penaltyService;
    private final MonitoringService monitoringService;
    private final PenaltyHistoryRepository penaltyHistoryRepository;

    public PenaltyController(
            PenaltyService penaltyService,
            MonitoringService monitoringService,
            PenaltyHistoryRepository penaltyHistoryRepository
    ) {
        this.penaltyService = Objects.requireNonNull(penaltyService);
        this.monitoringService = Objects.requireNonNull(monitoringService);
        this.penaltyHistoryRepository = Objects.requireNonNull(penaltyHistoryRepository);
    }

    public PenaltyCalculationResponseDto calculatePenalty(PenaltyCalculationRequestDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");

        BigDecimal total = penaltyService.calculatePenalty(
                dto.overstayed(),
                dto.extraHours(),
                dto.lostTicket(),
                dto.zoneMisuse(),
                dto.baseOverstayRatePerHour(),
                dto.overstayCap(),
                dto.lostTicketFee(),
                dto.misuseFee()
        );

        return new PenaltyCalculationResponseDto(total);
    }
    public ApplyPenaltyResponseDto applyPenalty(ApplyPenaltyRequestDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");

        PenaltyHistory history = penaltyHistoryRepository.getOrCreate(dto.userId());

        LocalDateTime timestamp = dto.timestamp();

        Penalty penalty = new Penalty(
                dto.type(),
                dto.amount(),
                timestamp
        );

        BlacklistStatus status =
                monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                        dto.userId(),
                        penalty,
                        history
                );

        penaltyHistoryRepository.save(dto.userId(), history);

        return new ApplyPenaltyResponseDto(
                dto.userId(),
                history.getTotalPenaltyAmount(),
                history.getPenaltyCount(),
                status
        );
    }
}