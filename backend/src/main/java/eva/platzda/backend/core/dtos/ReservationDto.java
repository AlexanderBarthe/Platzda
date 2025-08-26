package eva.platzda.backend.core.dtos;


import eva.platzda.backend.core.models.Reservation;
import eva.platzda.backend.core.models.User;

import java.time.LocalDateTime;

public class ReservationDto {

    private Long id;
    private Long userId;
    private int guests;
    private LocalDateTime start;
    private LocalDateTime end;

    public ReservationDto(Long id, Long userId, int guests, LocalDateTime start, LocalDateTime end){
        this.id = id;
        this.userId = userId;
        this.guests = guests;
        this.start = start;
        this.end = end;
    }

    public static ReservationDto toDto(Reservation reservation) {
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
