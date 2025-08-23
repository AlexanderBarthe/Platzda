package eva.platzda.backend.core.services;

import eva.platzda.backend.core.models.OpeningHours;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.models.Timeslot;
import eva.platzda.backend.core.repositories.TimeslotRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeslotGenerationService {

    private final TimeslotService timeslotService;
    private final TableService tableService;
    private final RestaurantService restaurantService;
    private final HoursService hoursService;
    private final TimeslotRepository timeslotRepository;

    @Autowired
    public TimeslotGenerationService(TimeslotService timeslotService, TableService tableService, RestaurantService restaurantService, HoursService hoursService, TimeslotRepository timeslotRepository) {
        this.tableService = tableService;
        this.timeslotService = timeslotService;
        this.restaurantService = restaurantService;
        this.hoursService = hoursService;
        this.timeslotRepository = timeslotRepository;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void publishTimeslotsScheduled() {
        publishTimeslots(LocalDate.now().plusWeeks(2));
    }

    public void publishTimeslots(LocalDate targetDate) {

        List<Restaurant> allRestaurants = restaurantService.findAllRestaurants();
        for(Restaurant r: allRestaurants) {
            List<Timeslot> slots = createTimeslots(r, targetDate);
            for(Timeslot s: slots) {
                for(RestaurantTable t: tableService.findAllTablesRestaurant(r.getId())) {
                    Timeslot saved = new Timeslot(t, s.getStartTime(), s.getEndTime(), null);
                    timeslotRepository.saveAndFlush(saved);
                }
            }
        }

        timeslotService.deleteTimeslotsBeforeDate(LocalDate.now().minusDays(1));

    }

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
