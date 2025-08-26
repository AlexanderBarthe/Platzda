package eva.platzda.backend.core.services;

import eva.platzda.backend.core.models.OpeningHours;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.models.Timeslot;
import eva.platzda.backend.core.repositories.TableRepository;
import eva.platzda.backend.core.repositories.TimeslotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private final HoursService hoursService;
    private final TimeslotRepository timeslotRepository;
    private final int WEEKS_PREGENERATED = 8;

    private final TableRepository tableRepository;
    /**
     * All Args Constructor
     * @param timeslotService
     * @param restaurantService
     * @param hoursService
     * @param timeslotRepository
     */
    @Autowired
    public TimeslotGenerationService(TimeslotService timeslotService, RestaurantService restaurantService, HoursService hoursService, TimeslotRepository timeslotRepository, TableRepository tableRepository) {
        this.timeslotService = timeslotService;
        this.restaurantService = restaurantService;
        this.hoursService = hoursService;
        this.timeslotRepository = timeslotRepository;
        this.tableRepository = tableRepository;
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
        List<OpeningHours> allHours = hoursService.findByWeekday(weekday.getValue(), r.getId());
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
}
