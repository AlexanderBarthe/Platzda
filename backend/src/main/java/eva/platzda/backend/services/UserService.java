package eva.platzda.backend.services;

import eva.platzda.backend.error_handling.NotFoundException;
import eva.platzda.backend.models.Restaurant;
import eva.platzda.backend.models.User;
import eva.platzda.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final RestaurantService restaurantService;

    @Autowired
    public UserService(UserRepository userRepository,  RestaurantService restaurantService) {
        this.userRepository = userRepository;
        this.restaurantService = restaurantService;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByName(username);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        if(!userRepository.existsById(id)) {
            throw new NotFoundException("User wit id " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public List<Restaurant> getFlagsOfUser(Long userId) {
        return findById(userId).getFlags();
    }

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
