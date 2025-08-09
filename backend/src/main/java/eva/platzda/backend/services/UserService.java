package eva.platzda.backend.services;

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
        userRepository.deleteById(id);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public List<Restaurant> getFlagsOfUser(Long userId) {
        return findById(userId).getFlags();
    }

    public void addFlag(Long userId, Long restaurantId) {

        User user = findById(userId);
        Restaurant restaurant = restaurantService.findById(restaurantId);

        user.addFlag(restaurant);

    }

    public void removeFlag(Long userId, Long restaurantId) {

        User user = findById(userId);
        Restaurant restaurant = restaurantService.findById(restaurantId);

        user.removeFlag(restaurant);
    }


}
