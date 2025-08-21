package eva.platzda.backend.core.services;


import eva.platzda.backend.core.models.OpeningHours;
import eva.platzda.backend.core.repositories.HoursRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HoursService {

    private final HoursRepository hoursRepository;

    public HoursService(HoursRepository hoursRepository) {
        this.hoursRepository = hoursRepository;
    }

    public OpeningHours findByWeekday(int weekday, Long restaurantId) {return hoursRepository.findByWeekday(weekday, restaurantId);}

    public List<OpeningHours> findAllOpeningHours(){return hoursRepository.findAll();}

    public List<OpeningHours> findByRestaurantId(Long restaurantId) {return hoursRepository.findByRestaurantId(restaurantId);}

    public OpeningHours findById(Long Id) {return hoursRepository.findById(Id).orElseThrow(() -> new RuntimeException("OpeningHours not found with id " + Id));}

    public OpeningHours createOpeningHours(OpeningHours hours) {return hoursRepository.save(hours);}

    public OpeningHours updateOpeningHours(OpeningHours hours) {return hoursRepository.save(hours);}

    public void deleteOpeningHoursById(Long id) {hoursRepository.deleteById(id);}

    public void deleteAllOpeningHours() {hoursRepository.deleteAll();}
}
