package com.eva.platzda.controllers;

import com.eva.platzda.models.Restaurant;
import com.eva.platzda.models.User;
import com.eva.platzda.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController("/user")
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
    public void create(@RequestBody User user) {
        user.setId(null);
        userService.saveUser(user);
    }

    @PutMapping
    public void update(@RequestBody User user) {
        if(userService.findById(user.getId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if(userService.findById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userService.deleteById(id);
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
