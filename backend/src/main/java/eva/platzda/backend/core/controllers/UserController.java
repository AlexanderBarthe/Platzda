package eva.platzda.backend.core.controllers;

import eva.platzda.backend.core.dtos.UserDto;
import eva.platzda.backend.core.models.User;
import eva.platzda.backend.core.services.UserService;
import eva.platzda.backend.error_handling.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Rest endpoints for managing users
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     *
     * Returns all users.
     *
     */
    @GetMapping()
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAll()
                .stream()
                .map(UserDto::fromObject)
                .collect(Collectors.toList()));
    }

    /**
     *
     * Returns user having given id
     *
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        User user = userService.findById(id);

        if(user == null) {
            throw new NotFoundException("User wit id " + id + " does not exist");
        }
        return ResponseEntity.ok(UserDto.fromObject(user));
    }

    /**
     *
     * Creates new user.
     *
     * @param user User with desired information
     * @return Saved User
     */
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody User user) {
        User u = userService.saveUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.fromObject(u));
    }

    /**
     *
     * Updates user information.
     *
     * @param user Reduced user model. Null values are kept original.
     * @return Saved User
     */
    @PutMapping
    public ResponseEntity<UserDto> update(@RequestBody User user) {
        User oldUser = userService.findById(user.getId());
        if(oldUser == null) throw new NotFoundException("User wit id " + user.getId() + " does not exist");

        if(user.getName() == null) user.setName(oldUser.getName());
        if(user.getEmail() == null) user.setEmail(oldUser.getEmail());
        if(user.getFlags() == null) user.setFlags(oldUser.getFlags());

        User u = userService.saveUser(user);
        return ResponseEntity.ok(UserDto.fromObject(u));
    }

    /**
     *
     * Deletes user with given id.
     *
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if(userService.findById(id) == null) {
            return ResponseEntity.noContent().build();
        }
        userService.deleteById(id);
        
        return ResponseEntity.ok().build();
    }

    /**
     *
     * Deletes all users.
     *
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        userService.deleteAll();
        return ResponseEntity.ok().build();
    }

    /**
     *
     * Returns a list of restaurant ids that flagged the user.
     *
     * @return Ids of restaurants
     */
    @GetMapping("/flags/{userId}")
    public ResponseEntity<List<Long>> flagsOfUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFlagsOfUser(userId)
                        .stream()
                        .map(r -> r.getId())
                        .collect(Collectors.toList()));
    }

    /**
     *
     * Adds a flag to a user.
     *
     * @param userId User that gets flagged
     * @param restaurantId Restaurant that flags the user
     * @return Updated User
     */
    @PutMapping("/flags/{userId}/{restaurantId}")
    public ResponseEntity<UserDto> addFlagToUser(@PathVariable Long userId, @PathVariable Long restaurantId) {
        User u = userService.addFlag(userId, restaurantId);
        return ResponseEntity.ok(UserDto.fromObject(u));
    }

    /**
     *
     * Removes a flag from a user.
     *
     * @param userId User to remove the flag
     * @param restaurantId Restaurant that unflags the user
     * @return Updated User
     */
    @DeleteMapping("/flags/{userId}/{restaurantId}")
    public ResponseEntity<Void> deleteFlagFromUser(@PathVariable Long userId, @PathVariable Long restaurantId) {
        userService.removeFlag(userId, restaurantId);
        return ResponseEntity.ok().build();
    }

}
