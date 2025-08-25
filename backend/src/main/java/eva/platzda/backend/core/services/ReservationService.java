package eva.platzda.backend.core.services;


import eva.platzda.backend.core.dtos.TimeWindow;
import eva.platzda.backend.core.models.*;
import eva.platzda.backend.core.repositories.*;
import eva.platzda.backend.error_handling.NotEnoughCapacityException;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ReservationService {

    private final TimeslotRepository timeslotRepository;
    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final HoursRepository hoursRepository;

    private static final int RES_SLOTS = 6; // jede Reservierung gilt für 90 min (6 Slots)

    public ReservationService(TimeslotRepository timeslotRepository,
                              TableRepository tableRepository,
                              UserRepository userRepository,
                              ReservationRepository reservationRepository,
                              HoursRepository hoursRepository) {
        this.timeslotRepository = timeslotRepository;
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.hoursRepository = hoursRepository;
    }

    private boolean isContinuous(List<Timeslot> window) {
        for(int i = 1; i < window.size(); i++) {
            if (!window.get(i).getStartTime().equals(window.get(i - 1).getEndTime())) {
                return false;
            }
        }
        return true;
    }

    @Async("reservationExecutor")
    public CompletableFuture<List<TimeWindow>> findFreeWindowsForTable(RestaurantTable table,
                                                                       LocalDateTime dayStart,
                                                                       LocalDateTime dayEnd) {
        List<Timeslot> ts = timeslotRepository
                .findAllForRestaurantAndDay(table.getRestaurant().getId(), dayStart, dayEnd)
                .stream()
                .filter(slot -> slot.getTable().equals(table))
                .toList();

        List<TimeWindow> windows = new ArrayList<>();

        for (int i = 0; i <= ts.size() - RES_SLOTS; i++) {
            List<Timeslot> window = ts.subList(i, i + RES_SLOTS);
            boolean allFree = window.stream().allMatch(s -> s.getUser() == null);
            if (allFree && isContinuous(window)) {
                Timeslot first = window.get(0);
                Timeslot last = window.getLast();

                TimeWindow tw = new TimeWindow(
                        first.getStartTime(),
                        last.getEndTime(),
                        table.getSize()
                );
                windows.add(tw);
            }
        }
        return CompletableFuture.completedFuture(windows);

    }

    public List<TimeWindow> findFreeSlots(Long restaurantId,
                                          LocalDate day,
                                          LocalTime open,
                                          LocalTime close,
                                          int guests) {
        LocalDateTime dayStart = day.atTime(open);
        LocalDateTime dayEnd = day.atTime(close);

        List<RestaurantTable> tables = tableRepository.findByRestaurantId(restaurantId);

        List<CompletableFuture<List<TimeWindow>>> futures = tables.stream()
                .map(t -> findFreeWindowsForTable(t, dayStart, dayEnd))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        List<TimeWindow> allWindows = futures.stream()
                .flatMap(f -> f.join().stream())
                .toList();

        Map<LocalDateTime, Integer> capByStart = new HashMap<>();
        Map<LocalDateTime, LocalDateTime> endByStart = new HashMap<>();

        for (TimeWindow w : allWindows) {
            capByStart.merge(w.start(), w.totalCapacity(), Integer::sum);
            endByStart.putIfAbsent(w.start(), w.end());
        }

        return capByStart.entrySet().stream()
                .filter(e -> e.getValue() >= guests)
                .map(e -> new TimeWindow(e.getKey(), endByStart.get(e.getKey()),e.getValue()))
                .sorted(Comparator.comparing(TimeWindow::start))
                .toList();
    }

    @Transactional
    public List<Reservation> bookSlot(Long restaurantId,
                                    Long userId,
                                    LocalDateTime start,
                                    int guests) {
        LocalDateTime end = start.plusMinutes(90);
        User user = userRepository.getReferenceById(userId);
        List<RestaurantTable> tables = tableRepository.findByRestaurantId(restaurantId);

        tables.sort(Comparator.comparing(RestaurantTable::getSize).reversed());

        int assigned = 0;
        List<Reservation> reservations = new ArrayList<>();

        for (RestaurantTable table: tables) {
            if (assigned >= guests) break;

            List<Timeslot> slots = timeslotRepository.findSlotsForUpdate(table, start, end);
            if (slots.size() < RES_SLOTS) continue;

            boolean allFree = slots.stream().allMatch(s -> s.getUser() == null);
            if (!allFree) continue;

            int take = Math.min(table.getSize(), guests -assigned);
            if (take > 0) {
                slots.forEach(s -> s.setUser(user));
                timeslotRepository.saveAll(slots);

               Reservation reservation = new Reservation(table, user, start, end, take);
               reservations.add(reservation);
                assigned += take;
            }
        }

        if (assigned < guests) {
            throw new NotEnoughCapacityException();
        }

        return reservationRepository.saveAll(reservations);
    }

    public List<Reservation> getReservationUser(User user) {
        return reservationRepository.findReservationForUser(user);
    }

    public List<Reservation> findReservationsForRestaurant(Restaurant restaurant, LocalDate date) {
        LocalDateTime dayStart = date.atTime(
                hoursRepository.findByWeekday(date.getDayOfWeek().getValue(), restaurant.getId()).getFirst().getOpeningTime());

        LocalDateTime dayEnd = date.atTime(
                hoursRepository.findByWeekday(date.getDayOfWeek().getValue(), restaurant.getId()).getLast().getClosingTime());

        return reservationRepository.findReservationsForRestaurant(restaurant.getId(), dayStart, dayEnd);
    }

    public void deleteReservation(Reservation reservation) {
        List<Timeslot> timeslots = timeslotRepository.findTimeslotsForReservation(reservation.getStartTime(), reservation.getEndTime(), reservation.getUser());
        for (Timeslot t: timeslots) {
            t.setUser(null);
        }
        reservationRepository.delete(reservation);
    }

    public Reservation findById(Long reservationId) {
        return reservationRepository.findById(reservationId).get();
    }

    public void deleteAllReservation(){
        for (Timeslot t: timeslotRepository.findAll()){
            t.setUser(null);
        }
        reservationRepository.deleteAll();
    }


    /*
    private final TableService tableService;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final TimeslotService timeslotService;


    public ReservationService(TableService tableService, UserService userService, RestaurantService restaurantService, TimeslotService timeslotService) {
        this.tableService = tableService;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.timeslotService = timeslotService;
    }

    public List<Timeslot> getFreeTimeslots(int persons, LocalDate date) {
        List<Timeslot> freeSlotsGeneral = timeslotService.findAllFreeTimeslots();
        List<Timeslot> freeSlotsDay = freeSlotsGeneral.stream()
                .filter(slot -> slot.getStartTime().toLocalDate().equals(date))
                .toList();
        return null;

    }

    public Boolean checkFullAvailability(Timeslot timeslot) {
        if(timeslot.getUser() != null) {
            return false;
        }
        for(int i = 1; i < 6; i++) {
            Timeslot toCheck = timeslotService.findSpecificTimeslot(
                    timeslot.getStartTime().plusMinutes(15*i),
                    timeslot.getEndTime().plusMinutes(15*i),
                    timeslot.getTable()).orElse(null);
            if((toCheck == null) || (toCheck.getUser() != null)) {
                return false;
            }
        }
        return true;
    } */

}
