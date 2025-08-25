package eva.platzda.backend.core.dtos;

import java.time.LocalDateTime;

public record TimeWindow(LocalDateTime start, LocalDateTime end, int totalCapacity) {
}