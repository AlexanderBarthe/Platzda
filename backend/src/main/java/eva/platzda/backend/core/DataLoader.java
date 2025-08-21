package eva.platzda.backend.core;

import eva.platzda.backend.core.models.OpeningHours;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.models.Timeslot;
import eva.platzda.backend.core.repositories.HoursRepository;
import eva.platzda.backend.core.repositories.RestaurantRepository;
import eva.platzda.backend.core.repositories.TableRepository;
import eva.platzda.backend.core.repositories.TimeslotRepository;
import eva.platzda.backend.core.services.TimeslotGenerationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

@Component
public class DataLoader implements CommandLineRunner {

    private final TableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;
    private final HoursRepository hoursRepository;
    private final TimeslotRepository timeslotRepository;
    private final TimeslotGenerationService service;


    public DataLoader(TableRepository tableRepository,
                      RestaurantRepository restaurantRepository,
                      HoursRepository hoursRepository,
                      TimeslotRepository timeslotRepository,
                      TimeslotGenerationService service) {
        this.tableRepository = tableRepository;
        this.restaurantRepository = restaurantRepository;
        this.hoursRepository = hoursRepository;
        this.timeslotRepository = timeslotRepository;
        this.service = service;
    }

    @Override
    public void run(String... args) throws Exception{
        if(restaurantRepository.count() == 0) {
            restaurantRepository.saveAndFlush(new Restaurant("tst1", null, 15));
            restaurantRepository.saveAndFlush(new Restaurant("tst2", null, 15));
            restaurantRepository.saveAndFlush(new Restaurant("tst3", null, 15));

        }
        if(tableRepository.count() == 0) {
            //tables Restaurant 1
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(1L),2));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(1L),2));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(1L),4));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(1L),10));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(1L),4));

            //tables Restaurant 2
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(2L),2));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(2L),2));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(2L),4));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(2L),10));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(2L),4));

            //tables Restaurant 3
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(3L),2));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(3L),2));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(3L),4));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(3L),10));
            tableRepository.save(new RestaurantTable(restaurantRepository.getReferenceById(3L),4));
        }
        if(hoursRepository.count() == 0) {
            for(int i = 1; i <8; i++) {
                for(Long j = 1L; j < 4L; j++) {
                    hoursRepository.save(new OpeningHours(restaurantRepository.getReferenceById(j),i, LocalTime.of(10,0), LocalTime.of(20,0)));
                }
            }
        }
        if(timeslotRepository.count() == 0) {
            timeslotRepository.save(new Timeslot(tableRepository.getReferenceById(1L), LocalDateTime.of(2025, Month.SEPTEMBER, 10, 10, 0), LocalDateTime.of(2025, Month.SEPTEMBER, 10, 10, 15), null));

        }
        //service.createTimeslots(restaurantRepository.getReferenceById(1L), LocalDate.of(2025, 10, 10));
        //service.publishTimeslots(LocalDate.of(2025,10,10));
        for (int i = 1; i <= 14; i++){
            service.publishTimeslots(LocalDate.now().plusDays(i));
        }
    }
}
