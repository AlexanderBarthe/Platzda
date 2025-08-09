package eva.platzda.backend.controllers;

import eva.platzda.backend.dtos.UserDto;
import eva.platzda.backend.error_handling.NotFoundException;
import eva.platzda.backend.models.Restaurant;
import eva.platzda.backend.models.User;
import eva.platzda.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAll()
                .stream()
                .map(UserDto::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        User user = userService.findById(id);

        if(user == null) {
            throw new NotFoundException("User wit id " + id + " does not exist");
        }
        return ResponseEntity.ok(UserDto.toDto(user));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody User user) {
        user.setId(null);
        User u = userService.saveUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.toDto(u));
    }

    @PutMapping
    public ResponseEntity<UserDto> update(@RequestBody User user) {
        User oldUser = userService.findById(user.getId());
        if(oldUser == null) throw new NotFoundException("User wit id " + user.getId() + " does not exist");

        if(user.getName() == null) user.setName(oldUser.getName());
        if(user.getEmail() == null) user.setEmail(oldUser.getEmail());
        if(user.getFlags() == null) user.setFlags(oldUser.getFlags());

        User u = userService.saveUser(user);
        return ResponseEntity.ok(UserDto.toDto(u));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if(userService.findById(id) == null) {
            return ResponseEntity.noContent().build();
        }
        userService.deleteById(id);
        
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        userService.deleteAll();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/flags/{userId}")
    public ResponseEntity<List<Long>> flagsOfUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFlagsOfUser(userId)
                        .stream()
                        .map(r -> r.getId())
                        .collect(Collectors.toList()));
    }

    @PutMapping("/flags/{userId}/{restaurantId}")
    public ResponseEntity<UserDto> addFlagToUser(@PathVariable Long userId, @PathVariable Long restaurantId) {
        User u = userService.addFlag(userId, restaurantId);
        return ResponseEntity.ok(UserDto.toDto(u));
    }

    @DeleteMapping("/flags/{userId}/{restaurantId}")
    public ResponseEntity<Void> deleteFlagFromUser(@PathVariable Long userId, @PathVariable Long restaurantId) {
        userService.removeFlag(userId, restaurantId);
        return ResponseEntity.ok().build();
    }

}
