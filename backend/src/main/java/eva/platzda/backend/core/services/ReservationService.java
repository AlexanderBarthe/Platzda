package eva.platzda.backend.core.services;


import eva.platzda.backend.core.dtos.TimeWindow;
import eva.platzda.backend.core.models.*;
import eva.platzda.backend.core.notifications.NotificationSocket;
import eva.platzda.backend.core.repositories.*;
import eva.platzda.backend.error_handling.NotEnoughCapacityException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final RestaurantRepository restaurantRepository;

    private final NotificationSocket notificationSocket;

    @Autowired
    public ReservationService(TimeslotRepository timeslotRepository,
                              TableRepository tableRepository,
                              UserRepository userRepository,
                              ReservationRepository reservationRepository,
                              HoursRepository hoursRepository,
                              RestaurantRepository restaurantRepository,
                              NotificationSocket notificationSocket) {
        this.timeslotRepository = timeslotRepository;
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.hoursRepository = hoursRepository;
        this.restaurantRepository = restaurantRepository;
        this.notificationSocket = notificationSocket;
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

        int res_slots = table.getRestaurant().getTimeSlotDuration();

        for (int i = 0; i <= ts.size() - res_slots; i++) {
            List<Timeslot> window = ts.subList(i, i + res_slots);
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

        tables.sort(Comparator.comparing(RestaurantTable::getSize)); //.reversed()

        int res_slots = restaurantRepository.getReferenceById(restaurantId).getTimeSlotDuration();

        int assigned = 0;
        List<Reservation> reservations = new ArrayList<>();

        List<RestaurantTable> availableTables = new ArrayList<>(tables);

        while ((assigned < guests) && (!availableTables.isEmpty())) {

            int missingSpace = guests - assigned;

            RestaurantTable selectedTable = availableTables.stream()
                    .filter(t -> t.getSize() >= (missingSpace))
                    .findFirst()
                    .orElse(null);

            if (selectedTable == null) {
                selectedTable = availableTables.getLast();
            }
            List<Timeslot> slots = timeslotRepository.findSlotsForUpdate(selectedTable, start, end);
            if (slots.size() < res_slots) {
                availableTables.remove(selectedTable);
                continue;
            }

            boolean allFree = slots.stream().allMatch(s -> s.getUser() == null);
            if (!allFree) {
                availableTables.remove(selectedTable);
                continue;
            }

            int take = Math.min(selectedTable.getSize(), guests-assigned);
            if (take > 0) {
                slots.forEach(s -> s.setUser(user));
                timeslotRepository.saveAll(slots);

                Reservation reservation = new Reservation(selectedTable, user, start, end, take);
                reservations.add(reservation);
                assigned += take;
            }
            availableTables.remove(selectedTable);
        }

        /*
        for (RestaurantTable table: tables) {
            if (assigned >= guests) break;

            List<Timeslot> slots = timeslotRepository.findSlotsForUpdate(table, start, end);
            if (slots.size() < res_slots) continue;

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
        }*/

        if (assigned < guests) {
            throw new NotEnoughCapacityException();
        }

        for (Reservation r: reservations) {
            String msg = "Reservation for table " + r.getRestaurantTable().getId() + " has been booked from " + r.getStartTime() + " to " + r.getEndTime();
            notificationSocket.notifyChange(r, msg);
        }

        return reservationRepository.saveAll(reservations);
    }

    public List<Reservation> getReservationUser(User user) {
        return reservationRepository.findReservationForUser(user);
    }

    public boolean checkSingleReservationDay(Long userId, LocalDate day) {
        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = day.atTime(23,59,59);
        return reservationRepository.existsReservationForUserOnDay(userId, dayStart, dayEnd);
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

        String msg = "Reservation for table " + reservation.getRestaurantTable().getId() + " from " + reservation.getStartTime() + " to " + reservation.getEndTime() + " has been canceled.";
        notificationSocket.notifyChange(reservation, msg);

        reservationRepository.delete(reservation);
    }

    public void deleteReservationUserDay(Long userId, LocalDate date) {
        List<Reservation> reservationsUser = reservationRepository.findReservationForUser(userRepository.getReferenceById(userId));
        for(Reservation r: reservationsUser) {
            if(r.getStartTime().toLocalDate().equals(date)) {
                deleteReservation(r);
            }
        }
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

}
