package eva.platzda.backend.core.dtos;

import java.time.LocalDateTime;

/**
 * Represents an available time window for reservations.
 *
 * Contains the start and end timestamps of the window,
 * as well as the total seating capacity available in that period.
 *
 * @param start Start time of the window
 * @param end End time of the window
 * @param totalCapacity Number of available seats in this window
 */
public record TimeWindow(LocalDateTime start, LocalDateTime end, int totalCapacity) {
}