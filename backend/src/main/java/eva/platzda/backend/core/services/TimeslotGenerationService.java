package eva.platzda.backend.core.services;

import eva.platzda.backend.core.models.OpeningHours;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.models.Timeslot;
import eva.platzda.backend.core.repositories.HoursRepository;
import eva.platzda.backend.core.repositories.TableRepository;
import eva.platzda.backend.core.repositories.TimeslotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating and publishing timeslots for restaurants.
 *
 * Creates timeslots for tables based on restaurant opening hours,
 * and deletes outdated timeslots. Supports scheduled and manual generation.
 */
@Service
public class TimeslotGenerationService {

    private final TimeslotService timeslotService;
    private final RestaurantService restaurantService;
    private final TimeslotRepository timeslotRepository;
    private final int WEEKS_PREGENERATED = 8;

    private final TableRepository tableRepository;
    private final HoursRepository hoursRepository;


    @Autowired
    public TimeslotGenerationService(TimeslotService timeslotService, RestaurantService restaurantService, TimeslotRepository timeslotRepository, TableRepository tableRepository, HoursRepository hoursRepository) {
        this.timeslotService = timeslotService;
        this.restaurantService = restaurantService;
        this.timeslotRepository = timeslotRepository;
        this.tableRepository = tableRepository;
        this.hoursRepository = hoursRepository;
    }

    public int getPregeneratedWeeks() {
        return WEEKS_PREGENERATED;
    }

    /**
     * Scheduled task to publish timeslots automatically.
     *
     * Runs daily at 1:00 AM and generates timeslots for restaurants
     * two weeks in advance.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void publishTimeslotsScheduled() {
        publishTimeslots(LocalDate.now().plusWeeks(WEEKS_PREGENERATED));
    }

    /**
     * Publishes timeslots for all restaurants on a specified target date.
     *
     * Creates timeslots for all tables in each restaurant based on
     * their opening hours, and removes outdated timeslots.
     *
     * @param targetDate Date for which to generate timeslots
     */
    public void publishTimeslots(LocalDate targetDate) {

        List<Restaurant> allRestaurants = restaurantService.findAllRestaurants();
        for(Restaurant r: allRestaurants) {
            List<Timeslot> slots = createTimeslots(r, targetDate);
            for(Timeslot s: slots) {
                for(RestaurantTable t: tableRepository.findByRestaurantId(r.getId())) {
                    Timeslot saved = new Timeslot(t, s.getStartTime(), s.getEndTime(), null);
                    timeslotRepository.saveAndFlush(saved);
                }
            }
        }

        timeslotService.deleteTimeslotsBeforeDate(LocalDate.now().minusDays(1));
    }

    public void connectTimeslotsTable(LocalDate targetDate, RestaurantTable table){
        List<Timeslot> slots = createTimeslots(table.getRestaurant(), targetDate);
        for(Timeslot s: slots) {
            Timeslot saved = new Timeslot(table, s.getStartTime(), s.getEndTime(), null );
            timeslotRepository.save(saved);
        }
    }

    /**
     * Creates timeslots for a single restaurant on a given date.
     *
     * Generates 15-minute interval timeslots between opening and closing times.
     *
     * @param r Restaurant for which timeslots should be created
     * @param date Date for the timeslots
     * @return List of generated Timeslot objects (without table assignments)
     */
    public List<Timeslot> createTimeslots(Restaurant r, LocalDate date) {
        List<Timeslot> slots = new ArrayList<>();

        DayOfWeek weekday = date.getDayOfWeek();
        List<OpeningHours> allHours = hoursRepository.findByWeekday(weekday.getValue(), r.getId());
        if (allHours == null) {
            return new ArrayList<>();
        }

        for(OpeningHours hours : allHours) {
            LocalDateTime current = LocalDateTime.of(date, hours.getOpeningTime());
            LocalDateTime closingTime = LocalDateTime.of(date, hours.getClosingTime());
            while (current.isBefore(closingTime)) {
                LocalDateTime next = current.plusMinutes(15);
                if (next.isAfter(closingTime)) {
                    break;
                }
                slots.add(new Timeslot(current, next));
                current = next;
            }
        }

        return slots;
    }

    /**
     *
     * Adds or removes timeslots for updated opening hours.
     * Won't affect timeslots booked by users
     *
     * @param restaurant Restaurant with new opening hours.
     */
    @Transactional
    public void updateTimeslots(Restaurant restaurant) {
        Objects.requireNonNull(restaurant, "restaurant must not be null");

        List<OpeningHours> openingHours = hoursRepository.findByRestaurantId(restaurant.getId());
        List<Timeslot> existingSlots = timeslotRepository.findByRestaurantId(restaurant.getId());
        List<RestaurantTable> tables = tableRepository.findByRestaurantId(restaurant.getId());

        if (tables.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate minDate = existingSlots.stream()
                .map(ts -> ts.getStartTime().toLocalDate())
                .min(Comparator.naturalOrder())
                .orElse(today);

        LocalDate startDate = minDate.isBefore(today) ? minDate : today;
        LocalDate endDate = today.plusDays(WEEKS_PREGENERATED*7);

        //Map weekday -> OpeningHours (for multiple entries per weekday)
        Map<Integer, List<OpeningHours>> ohByWeekday = openingHours.stream()
                .collect(Collectors.groupingBy(OpeningHours::getWeekday));

        //Build expected start times per date (same for all tables)
        Set<LocalDateTime> expectedStarts = new HashSet<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int weekday = date.getDayOfWeek().getValue(); //Monday=1 ... Sunday=7
            List<OpeningHours> ohList = ohByWeekday.getOrDefault(weekday, Collections.emptyList());
            for (OpeningHours oh : ohList) {
                List<LocalDateTime> starts = generateStartsForOpening(date, oh);
                expectedStarts.addAll(starts);
            }
        }

        //Index existing slots
        Map<Long, Map<LocalDateTime, Timeslot>> existingByTable = new HashMap<>();
        for (RestaurantTable table : tables) {
            existingByTable.put(table.getId(), new HashMap<>());
        }
        for (Timeslot ts : existingSlots) {
            if (ts.getTable() == null || ts.getStartTime() == null) continue;
            Long tableId = ts.getTable().getId();
            if (!existingByTable.containsKey(tableId)) continue;
            existingByTable.get(tableId).put(ts.getStartTime(), ts);
        }

        List<Timeslot> toCreate = new ArrayList<>();
        List<Timeslot> toDelete = new ArrayList<>();

        //Compare expected vs existing starts
        for (RestaurantTable table : tables) {
            Map<LocalDateTime, Timeslot> existingForTable = existingByTable.getOrDefault(table.getId(), Collections.emptyMap());

            //Create missing starts
            for (LocalDateTime start : expectedStarts) {
                if (!existingForTable.containsKey(start)) {
                    Timeslot newTs = new Timeslot();
                    newTs.setTable(table);
                    newTs.setStartTime(start);
                    newTs.setEndTime(start.plusMinutes(15));
                    newTs.setUser(null);
                    toCreate.add(newTs);
                }
            }

            //Deleting slots not within opening hours
            for (Map.Entry<LocalDateTime, Timeslot> entry : existingForTable.entrySet()) {
                LocalDateTime start = entry.getKey();
                Timeslot ts = entry.getValue();
                if (!expectedStarts.contains(start)) {
                    //Not deleting if slot is booked
                    if (ts.getUser() == null) {
                        toDelete.add(ts);
                    }
                }
            }
        }

        // Persist
        if (!toDelete.isEmpty()) {
            timeslotRepository.deleteAll(toDelete);
        }
        if (!toCreate.isEmpty()) {
            timeslotRepository.saveAll(toCreate);
        }
    }

    /**
     *
     * Generates Opening-Hours-Interval for a date in quarter-hour slots
     *
     */
    private List<LocalDateTime> generateStartsForOpening(LocalDate date, OpeningHours oh) {
        List<LocalDateTime> result = new ArrayList<>();
        LocalTime open = oh.getOpeningTime();
        LocalTime close = oh.getClosingTime();

        if (open == null || close == null) return result;

        // Wenn close <= open => kein Intervall (oder ggf. über Mitternacht nicht unterstützt)
        if (!close.isAfter(open)) {
            return result;
        }

        //catching cases where times are not quarter-hours
        LocalTime cursor = alignToQuarter(open);
        LocalDateTime dtCursor = LocalDateTime.of(date, cursor);
        LocalDateTime dtClose = LocalDateTime.of(date, close);

        while (!dtCursor.plusMinutes(15).isAfter(dtClose)) { //ensure slot fits
            result.add(dtCursor);
            dtCursor = dtCursor.plusMinutes(15);
        }
        return result;
    }

    private LocalTime alignToQuarter(LocalTime t) {
        int minute = t.getMinute();
        int quarters = (minute + 7) / 15;
        int aligned = quarters * 15;
        if (aligned >= 60) aligned = 45; //Fallback
        return LocalTime.of(t.getHour(), aligned, 0);
    }
}
