package eva.platzda.backend.core.services;


import eva.platzda.backend.core.models.OpeningHours;
import eva.platzda.backend.core.models.Reservation;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.repositories.HoursRepository;
import eva.platzda.backend.core.repositories.RestaurantRepository;
import eva.platzda.backend.core.repositories.TimeslotRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.List;

/**
 * Service for managing opening hours of restaurants.
 *
 * Provides methods to create, read, update, and delete opening hours.
 */
@Service
public class HoursService {

    private final TimeslotGenerationService timeslotGenerationService;

    private final HoursRepository hoursRepository;

    private final EntityManager em;
    private final TimeslotRepository timeslotRepository;

    @Autowired
    public HoursService(HoursRepository hoursRepository, TimeslotGenerationService timeslotGenerationService, EntityManager em, TimeslotRepository timeslotRepository) {
        this.hoursRepository = hoursRepository;
        this.timeslotGenerationService = timeslotGenerationService;
        this.em = em;
        this.timeslotRepository = timeslotRepository;
    }

    /**
     * Finds all opening hours for a given weekday and restaurant.
     *
     * @param weekday Weekday (1 = Monday, 7 = Sunday)
     * @param restaurantId ID of the restaurant
     * @return List of opening hours for the specified weekday and restaurant
     */
    public List<OpeningHours> findByWeekday(int weekday, Long restaurantId) {return hoursRepository.findByWeekday(weekday, restaurantId);}

    /**
     * Returns all opening hours in the system.
     *
     * @return List of all opening hours
     */
    public List<OpeningHours> findAllOpeningHours(){return hoursRepository.findAll();}

    /**
     * Finds all opening hours for a specific restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @return List of opening hours for the restaurant
     */
    public List<OpeningHours> findByRestaurantId(Long restaurantId) {return hoursRepository.findByRestaurantId(restaurantId);}

    /**
     * Finds an opening hours entry by its ID.
     *
     * @param Id ID of the opening hours entry
     * @return The OpeningHours entity
     * @throws RuntimeException if no entry is found
     */
    public OpeningHours findById(Long Id) {return hoursRepository.findById(Id).orElseThrow(() -> new RuntimeException("OpeningHours not found with id " + Id));}

    /**
     * Creates a new opening hours entry.
     *
     * @param hours OpeningHours entity to create
     * @return The created OpeningHours entity
     */
    @Transactional
    public OpeningHours createOpeningHours(OpeningHours hours) {
        hours.setId(null);
        OpeningHours saved = hoursRepository.save(hours);

        timeslotGenerationService.updateTimeslots(hours.getRestaurant());

        return saved;
    }


    /**
     * Updates an existing opening hours entry.
     *
     * @param hours OpeningHours entity to update
     * @return The updated OpeningHours entity
     */
    @Transactional
    public OpeningHours updateOpeningHours(OpeningHours hours) {

        OpeningHours saved = hoursRepository.save(hours);
        timeslotGenerationService.updateTimeslots(hours.getRestaurant());
        return saved;
    }

    /**
     * Deletes an opening hours entry by its ID.
     *
     * @param id ID of the entry to delete
     */
    @Transactional
    public void deleteOpeningHoursById(Long id) {
        Restaurant restaurant = hoursRepository.findById(id).get().getRestaurant();

        hoursRepository.deleteById(id);
        timeslotGenerationService.updateTimeslots(restaurant);
    }

    /**
     * Deletes all opening hours for a specific restaurant.
     *
     * @param restaurantId ID of the restaurant
     */
    @Transactional
    public void deleteOpeningHoursOfRestaurant(Long restaurantId) {
        Restaurant restaurant = hoursRepository.findById(restaurantId).get().getRestaurant();
        hoursRepository.deleteByRestaurantId(restaurantId);
        timeslotGenerationService.updateTimeslots(restaurant);
    }

    /**
     * Deletes all opening hours in the system.
     */
    @Transactional
    public void deleteAllOpeningHours() {
        hoursRepository.deleteAll();
        em.createNativeQuery("ALTER TABLE opening_hours ALTER COLUMN id RESTART WITH 1").executeUpdate();
        timeslotRepository.deleteAll();
    }
}
