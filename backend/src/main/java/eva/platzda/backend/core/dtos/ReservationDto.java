package eva.platzda.backend.core.dtos;


import eva.platzda.backend.core.models.Reservation;

import java.time.LocalDateTime;

/**
 * Dto for wrapping up reservations as their ids
 */
public class ReservationDto {

    private Long id;
    private Long userId;
    private int guests;
    private LocalDateTime start;
    private LocalDateTime end;

    /**
     * All Args Constructor
     *
     * @param id ID of the reservation
     * @param userId ID of the user who made the reservation
     * @param guests Number of guests
     * @param start Start time of the reservation
     * @param end End time of the reservation
     */
    public ReservationDto(Long id, Long userId, int guests, LocalDateTime start, LocalDateTime end){
        this.id = id;
        this.userId = userId;
        this.guests = guests;
        this.start = start;
        this.end = end;
    }

    /**
     * Converts a Reservation entity into a ReservationDto.
     *
     * @param reservation Reservation entity
     * @return ReservationDto with mapped values
     */
    public static ReservationDto fromObject(Reservation reservation) {
        return new ReservationDto(reservation.getId(),
                reservation.getUser().getId(),
                reservation.getNumberOfGuests(),
                reservation.getStartTime(),
                reservation.getEndTime());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUser() {
        return userId;
    }

    public void setUser(Long userId) {
        this.userId = userId;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
}
