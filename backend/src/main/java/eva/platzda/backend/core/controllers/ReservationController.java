package eva.platzda.backend.core.controllers;

import eva.platzda.backend.core.dtos.ReservationDto;
import eva.platzda.backend.core.dtos.TimeWindow;
import eva.platzda.backend.core.models.Reservation;
import eva.platzda.backend.core.services.HoursService;
import eva.platzda.backend.core.services.ReservationService;
import eva.platzda.backend.core.services.RestaurantService;
import eva.platzda.backend.core.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reservation")
public class ReservationController {


    private ReservationService reservationService;
    private RestaurantService restaurantService;
    private HoursService hoursService;
    private UserService userService;

    @Autowired
    public ReservationController(ReservationService reservationService,
                                 RestaurantService restaurantService,
                                 HoursService hoursService,
                                 UserService userService) {
        this.reservationService = reservationService;
        this.restaurantService = restaurantService;
        this.hoursService = hoursService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<List<ReservationDto>> createReservation(@RequestParam Long restaurantId,
                                               @RequestParam Long userId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                               @RequestParam int guests) {

        if(reservationService.checkSingleReservationDay(userId, start.toLocalDate())) {
            throw new IllegalStateException("User already has a reservation on this day");
        }

        List<ReservationDto> reservationDtos = reservationService.bookSlot(restaurantId, userId, start, guests).stream().map(ReservationDto::toDto).toList();

        return ResponseEntity.ok(reservationDtos);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReservationDto>> getReservationsForRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {

        List<Reservation> reservation = reservationService.findReservationsForRestaurant(restaurantService.findById(restaurantId), day);

        Map<Long, List<Reservation>> groupedByUser = reservation.stream()
                .collect(Collectors.groupingBy(res -> res.getUser().getId()));

        List<ReservationDto> result = groupedByUser.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    List<Reservation> userReservations = entry.getValue();

                    int totalGuests = userReservations.stream()
                            .mapToInt(Reservation::getNumberOfGuests)
                            .sum();
                    Reservation first = userReservations.get(0);

                    return new ReservationDto(
                            userId,
                            totalGuests,
                            first.getStartTime(),
                            first.getEndTime()
                    );
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/restaurant/{restaurantId}/free-slots")
    public ResponseEntity<List<TimeWindow>> getFreeSlots(@PathVariable Long restaurantId,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
                                         @RequestParam int guests) {
        LocalTime open = hoursService.findByWeekday(day.getDayOfWeek().getValue(), restaurantId).getFirst().getOpeningTime();
        LocalTime close = hoursService.findByWeekday(day.getDayOfWeek().getValue(), restaurantId).getLast().getClosingTime();
        return ResponseEntity.ok(reservationService.findFreeSlots(restaurantId, day, open, close, guests));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<String> deleteReservationId(@PathVariable Long reservationId) {
        reservationService.deleteReservation(reservationService.findById(reservationId));
        return ResponseEntity.ok("Reservation deleted succesfully");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteReservationUser(@PathVariable Long userId, @RequestParam LocalDate date){
        reservationService.deleteReservationUserDay(userId, date);
        return ResponseEntity.ok("Reservation deleted succesfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllReservation(){
        reservationService.deleteAllReservation();
        return ResponseEntity.ok("All Reservations deleted");
    }


}
