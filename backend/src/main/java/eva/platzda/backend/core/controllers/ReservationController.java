package eva.platzda.backend.core.controllers;

import eva.platzda.backend.core.dtos.ReservationDto;
import eva.platzda.backend.core.dtos.TimeWindow;
import eva.platzda.backend.core.models.Reservation;
import eva.platzda.backend.core.services.HoursService;
import eva.platzda.backend.core.services.ReservationService;
import eva.platzda.backend.core.services.RestaurantService;
import eva.platzda.backend.core.services.UserService;
import eva.platzda.backend.error_handling.TooManyBookingsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for managing reservations.
 */
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

    /**
     * Creates a new reservation for a user at a restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @param userId ID of the user
     * @param start Start time of the reservation
     * @param guests Number of guests
     * @return List of created reservations as DTOs
     * @throws TooManyBookingsException if the user already has a reservation on the same day
     */
    @PostMapping
    public ResponseEntity<List<ReservationDto>> createReservation(@RequestParam Long restaurantId,
                                               @RequestParam Long userId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                               @RequestParam int guests) {

        if(reservationService.checkSingleReservationDay(userId, start.toLocalDate())) {
            throw new TooManyBookingsException("User already has a reservation on this day");
        }

        List<ReservationDto> reservationDtos = reservationService.bookSlot(restaurantId, userId, start, guests).stream().map(ReservationDto::fromObject).toList();

        return ResponseEntity.ok(reservationDtos);
    }

    /**
     * Returns all reservations for a given restaurant and day.
     *
     * @param restaurantId ID of the restaurant
     * @param day Day of the reservations
     * @return List of reservations as DTOs
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReservationDto>> getReservationsForRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {

        List<Reservation> reservation = reservationService.findReservationsForRestaurant(restaurantService.findById(restaurantId), day);

        List<ReservationDto> dtos = reservation.stream()
                .map(ReservationDto::fromObject)
                .sorted(Comparator.comparing(ReservationDto::getUser).thenComparing(ReservationDto::getStart))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);

        /*
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

        //List<ReservationDto> result = reservation.stream().map(ReservationDto::toDto).toList();
        return ResponseEntity.ok(result);*/
    }

    /**
     * Returns all free reservation slots for a given restaurant, day, and number of guests.
     *
     * @param restaurantId ID of the restaurant
     * @param day Day for which free slots are requested
     * @param guests Number of guests
     * @return List of available time windows
     */
    @GetMapping("/restaurant/{restaurantId}/free-slots")
    public ResponseEntity<List<TimeWindow>> getFreeSlots(@PathVariable Long restaurantId,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
                                         @RequestParam int guests) {
        LocalTime open = hoursService.findByWeekday(day.getDayOfWeek().getValue(), restaurantId).getFirst().getOpeningTime();
        LocalTime close = hoursService.findByWeekday(day.getDayOfWeek().getValue(), restaurantId).getLast().getClosingTime();
        return ResponseEntity.ok(reservationService.findFreeSlots(restaurantId, day, open, close, guests));
    }

    /**
     * Deletes a reservation by its ID.
     *
     * @param reservationId ID of the reservation
     * @return Confirmation message
     */
    @DeleteMapping("/id/{reservationId}")
    public ResponseEntity<String> deleteReservationId(@PathVariable Long reservationId) {
        reservationService.deleteReservation(reservationService.findById(reservationId));
        return ResponseEntity.ok("Reservation deleted succesfully");
    }

    /**
     * Deletes a reservation of a user on a specific day.
     *
     * @param userId ID of the user
     * @param date Date of the reservation
     * @return Confirmation message
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deleteReservationUser(@PathVariable Long userId, @RequestParam LocalDate date){
        reservationService.deleteReservationUserDay(userId, date);
        return ResponseEntity.ok("Reservation deleted succesfully");
    }

    /**
     * Deletes all reservations in the system.
     *
     * @return Confirmation message
     */
    @DeleteMapping
    public ResponseEntity<String> deleteAllReservation(){
        reservationService.deleteAllReservation();
        return ResponseEntity.ok("All Reservations deleted");
    }


}
