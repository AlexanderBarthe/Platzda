package eva.platzda.backend.controllers;

import eva.platzda.backend.dtos.UserDto;
import eva.platzda.backend.models.Restaurant;
import eva.platzda.backend.models.User;
import eva.platzda.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public List<UserDto> findAll() {
        return userService.findAll()
                .stream()
                .map(UserDto::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        User user = userService.findById(id);

        if(user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return UserDto.toDto(user);
    }

    @PostMapping
    public UserDto create(@RequestBody User user) {
        user.setId(null);
        User u = userService.saveUser(user);

        return UserDto.toDto(u);
    }

    @PutMapping
    public UserDto update(@RequestBody User user) {
        User oldUser = userService.findById(user.getId());
        if(oldUser == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        if(user.getName() == null) user.setName(oldUser.getName());
        if(user.getEmail() == null) user.setEmail(oldUser.getEmail());
        if(user.getFlags() == null) user.setFlags(oldUser.getFlags());

        User u = userService.saveUser(user);
        return UserDto.toDto(u);
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

    @PutMapping("/flags/{userId}/{restaurantId}")
    public UserDto addFlagToUser(@PathVariable Long userId, @PathVariable Long restaurantId) {
        User u = userService.addFlag(userId, restaurantId);
        return UserDto.toDto(u);
    }

    @DeleteMapping("/flags/{userId}/{restaurantId}")
    public void deleteFlagFromUser(@PathVariable Long userId, @PathVariable Long restaurantId) {
        userService.removeFlag(userId, restaurantId);
    }

}
