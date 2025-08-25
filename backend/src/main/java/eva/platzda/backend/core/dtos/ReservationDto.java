package eva.platzda.backend.core.dtos;


import eva.platzda.backend.core.models.Reservation;
import eva.platzda.backend.core.models.User;

import java.time.LocalDateTime;

public class ReservationDto {

    private User user;
    private int guests;
    private LocalDateTime start;
    private LocalDateTime end;

    public ReservationDto(User user, int guests, LocalDateTime start, LocalDateTime end){
        this.user = user;
        this.guests = guests;
        this.start = start;
        this.end = end;
    }

    public static ReservationDto toDto(Reservation reservation) {
        return new ReservationDto(reservation.getUser(),
                reservation.getNumberOfGuests(),
                reservation.getStartTime(),
                reservation.getEndTime());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
