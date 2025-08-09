package eva.platzda.backend.controllers;

import eva.platzda.backend.models.Restaurant;
import eva.platzda.backend.models.User;
import eva.platzda.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        User user = userService.findById(id);

        if(user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        user.setId(null);
        return userService.saveUser(user);
    }

    @PutMapping
    public User update(@RequestBody User user) {
        User oldUser = userService.findById(user.getId());
        if(oldUser == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        if(user.getName() == null) user.setName(oldUser.getName());
        if(user.getEmail() == null) user.setEmail(oldUser.getEmail());
        if(user.getFlags() == null) user.setFlags(oldUser.getFlags());

        return userService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if(userService.findById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userService.deleteById(id);
    }

    @DeleteMapping
    public void deleteAll() {
        userService.deleteAll();
    }

    @GetMapping("/flags/{userId}")
    public List<Restaurant> flagsOfUser(@PathVariable Long userId) {
        return userService.getFlagsOfUser(userId);
    }

    @PostMapping("/flags/{userId}/{restaurantId}")
    public void addFlagToUser(@PathVariable Long userId, @PathVariable Long restaurantId) {
        userService.addFlag(userId, restaurantId);
    }

    @DeleteMapping("/flags/{userId}/{restaurantId}")
    public void deleteFlagFromUser(@PathVariable Long userId, @PathVariable Long restaurantId) {
        userService.removeFlag(userId, restaurantId);
    }

}
