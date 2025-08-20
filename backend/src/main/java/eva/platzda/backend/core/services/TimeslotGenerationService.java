package eva.platzda.backend.core.services;

import eva.platzda.backend.core.models.OpeningHours;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.models.Timeslot;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeslotGenerationService {

    private final TimeslotService timeslotService;
    private final TableService tableService;
    private final RestaurantService restaurantService;
    private final HoursService hoursService;

    public TimeslotGenerationService(TimeslotService timeslotService, TableService tableService, RestaurantService restaurantService, HoursService hoursService) {
        this.tableService = tableService;
        this.timeslotService = timeslotService;
        this.restaurantService = restaurantService;
        this.hoursService = hoursService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void publishTimeslotsScheduled() {
        publishTimeslots(LocalDate.now().plusWeeks(2));
    }

    @PostConstruct
    public void init() {
        for (int i = 1; i < 14; i++){
            publishTimeslots(LocalDate.now().plusDays(i));
        }
    }

    public void publishTimeslots(LocalDate targetDate) {

        List<Restaurant> allRestaurants = restaurantService.findAllRestaurants();
        for(Restaurant r: allRestaurants) {
            List<Timeslot> slots = createTimeslots(r, targetDate);
            for(RestaurantTable t: tableService.findAllTablesRestaurant(r.getId())) {
                for(Timeslot s: slots) {
                    s.setTable(t);
                    timeslotService.createTimeslot(s);
                }
            }
        }

        timeslotService.deleteTimeslotsBeforeDate(LocalDate.now().minusDays(1));

    }

    public List<Timeslot> createTimeslots(Restaurant r, LocalDate date) {
        List<Timeslot> slots = new ArrayList<>();

        DayOfWeek weekday = date.getDayOfWeek();
        OpeningHours hours = hoursService.findByWeekday(weekday.getValue(), r.getId());
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

        return slots;
    }
}
