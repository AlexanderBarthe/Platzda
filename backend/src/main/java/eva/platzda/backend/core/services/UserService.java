package eva.platzda.backend.core.services;

import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.models.User;
import eva.platzda.backend.core.repositories.UserRepository;
import eva.platzda.backend.error_handling.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for Users
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    private final RestaurantService restaurantService;

    @Autowired
    public UserService(UserRepository userRepository,  RestaurantService restaurantService) {
        this.userRepository = userRepository;
        this.restaurantService = restaurantService;
    }

    /**
     *
     * Returns all Users.
     *
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     *
     * Returns all Users with given id
     *
     */
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     *
     * Saves a User with similar id.
     *
     * @param user User with updated information
     * @return Saved User
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     *
     * Deletes User with given id.
     *
     */
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     *
     * Deletes all Users.
     *
     */
    public void deleteAll() {
        userRepository.deleteAll();
    }

    /**
     *
     * Returns Restaurants that flagged a User.
     *
     * @param userId Id of User
     * @return List of flagging Restaurants
     */
    public List<Restaurant> getFlagsOfUser(Long userId) {
        return findById(userId).getFlags();
    }

    /**
     *
     * Adds flag to a user.
     *
     * @param userId Id of flagged User
     * @param restaurantId Id of flagging Restaurant
     * @return Updated User
     */
    public User addFlag(Long userId, Long restaurantId) {

        User user = findById(userId);
        Restaurant restaurant = restaurantService.findById(restaurantId);

        if(user == null) {
            throw new NotFoundException("User wit id " + userId + " not found");
        }
        if(restaurant == null) {
            throw new NotFoundException("Restaurant wit id " + restaurantId + " not found");
        }

        user.addFlag(restaurant);

        return userRepository.save(user);
    }

    /**
     *
     * Removes flag from a User.
     *
     * @param userId Id of User to remove flag from
     * @param restaurantId Flag Id that gets removed.
     */
    public void removeFlag(Long userId, Long restaurantId) {

        User user = findById(userId);
        Restaurant restaurant = restaurantService.findById(restaurantId);

        if(user == null) {
            throw new NotFoundException("User wit id " + userId + " not found");
        }
        if(restaurant == null) {
            throw new NotFoundException("Restaurant wit id " + restaurantId + " not found");
        }

        user.removeFlag(restaurant);

        userRepository.save(user);
    }


}
