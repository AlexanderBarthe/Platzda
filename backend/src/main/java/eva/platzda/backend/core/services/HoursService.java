package eva.platzda.backend.core.services;


import eva.platzda.backend.core.models.OpeningHours;
import eva.platzda.backend.core.repositories.HoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing opening hours of restaurants.
 *
 * Provides methods to create, read, update, and delete opening hours.
 */
@Service
public class HoursService {

    private final HoursRepository hoursRepository;

    /**
     * Constructs a new HoursService with the given repository.
     *
     * @param hoursRepository Repository for OpeningHours entities
     */
    public HoursService(HoursRepository hoursRepository) {
        this.hoursRepository = hoursRepository;
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
        return hoursRepository.save(hours);
    }


    /**
     * Updates an existing opening hours entry.
     *
     * @param hours OpeningHours entity to update
     * @return The updated OpeningHours entity
     */
    @Transactional
    public OpeningHours updateOpeningHours(OpeningHours hours) {return hoursRepository.save(hours);}

    /**
     * Deletes an opening hours entry by its ID.
     *
     * @param id ID of the entry to delete
     */
    @Transactional
    public void deleteOpeningHoursById(Long id) {hoursRepository.deleteById(id);}

    /**
     * Deletes all opening hours for a specific restaurant.
     *
     * @param restaurantId ID of the restaurant
     */
    @Transactional
    public void deleteOpeningHoursOfRestaurant(Long restaurantId) {
        hoursRepository.deleteByRestaurantId(restaurantId);
    }

    /**
     * Deletes all opening hours in the system.
     */
    @Transactional
    public void deleteAllOpeningHours() {hoursRepository.deleteAll();}
}
