package eva.platzda.backend.core.services;

import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.repositories.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing Restaurants.
 *
 * Provides CRUD operations for Restaurant entities.
 */
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    /**
     *
     * Retrieves all restaurants.
     *
     */
    public List<Restaurant> findAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     *
     * Finds a restaurant by its id.
     *
     * @param id Id of the restaurant
     * @return Restaurant entity or null if not found
     */
    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
    }


    public List<Restaurant> findByTags(String taglist) {

        List<String> tags = List.of(taglist.split(","));
        return findAllRestaurants()
                .stream()
                .filter(restaurant -> restaurant.getTags().containsAll(tags))
                .toList();
    }

    /**
     *
     * Creates a new restaurant.
     *
     * @param restaurant Restaurant entity to create
     * @return Created Restaurant entity
     */
    public Restaurant createRestaurant(Restaurant restaurant) {
        restaurant.setId(null);
        return restaurantRepository.save(restaurant);
    }

    /**
     *
     * Updates an existing restaurant.
     *
     * @param restaurant Restaurant entity with updated data
     * @return Updated Restaurant entity
     */
    public Restaurant updateRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    /**
     *
     * Deletes a restaurant by its id.
     *
     * @param id Id of the restaurant to delete
     */
    public void deleteRestaurantById(Long id) {
        restaurantRepository.deleteById(id);
    }

    /**
     *
     * Deletes all restaurants.
     *
     */
    public void deleteAllRestaurants() {
        restaurantRepository.deleteAll();
    }

}
