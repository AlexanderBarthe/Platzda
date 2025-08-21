package eva.platzda.backend.core.services;


import eva.platzda.backend.core.models.Timeslot;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

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
        List<Timeslot> freeSlotsGeneral = timeslotService.finAllFreeTimeslots();
        List<Timeslot> freeSlotsDay = freeSlotsGeneral.stream()
                .filter(slot -> slot.getStartTime().toLocalDate().equals(date))
                .toList();
        return null;

    }

}
