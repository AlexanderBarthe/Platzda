package com.eva.platzda.controllers;

import com.eva.platzda.models.Restaurant;
import com.eva.platzda.models.User;
import com.eva.platzda.services.RestaurantService;
import com.eva.platzda.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController("/restaurant")
public class RestaurantController {

    private UserService userService;
    private RestaurantService restaurantService;

    @Autowired
    public RestaurantController(UserService userService, RestaurantService restaurantService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public List<Restaurant> getRestaurants() {
        return restaurantService.findAllRestaurants();
    }

    @GetMapping("/{id}")
    public Restaurant getRestaurant(@PathVariable Long id) {
        return restaurantService.findById(id);
    }

    @PostMapping("/{ownerId}")
    public void createRestaurant(@PathVariable Long ownerId, @RequestBody Restaurant restaurant) {
        restaurant.setId(null);
        if(restaurant.getAddress() == null) restaurant.setAddress("");
        if(restaurant.getTimeSlotDuration() == null) restaurant.setTimeSlotDuration(90);


        User owner = userService.findById(ownerId);
        if(owner == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }
        else {
            restaurant.setOwner(owner);
        }

        restaurantService.createRestaurant(restaurant);

    }

    @PutMapping("/{restaurantId}/owner/{ownerId}")
    public void updateOwner(@PathVariable Long restaurantId, @PathVariable Long ownerId) {

        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");

        User newOwner = userService.findById(ownerId);
        if(newOwner == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        restaurant.setOwner(newOwner);

        restaurantService.updateRestaurant(restaurant);

    }

    @PutMapping("/{restaurantId}")
    public void updateRestaurant(@PathVariable Long restaurantId, @RequestBody Restaurant restaurant) {
        Restaurant oldRestaurant = restaurantService.findById(restaurantId);
        if(oldRestaurant == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");

        if(restaurant.getAddress() == null) restaurant.setAddress(oldRestaurant.getAddress());
        if(restaurant.getTimeSlotDuration() == null) restaurant.setTimeSlotDuration(oldRestaurant.getTimeSlotDuration());
        if(restaurant.getTags() == null) restaurant.setTags(oldRestaurant.getTags());

        restaurantService.updateRestaurant(restaurant);
    }

    @DeleteMapping("/{restaurantId}")
    public void deleteRestaurant(@PathVariable Long restaurantId) {
        if(restaurantService.findById(restaurantId) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");
        restaurantService.deleteRestaurantById(restaurantId);
    }

    @PostMapping("{restaurantId}/tags/{tagname}")
    public void addTag(@PathVariable Long restaurantId, @PathVariable String tagname) {
        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");

        restaurant.addTag(tagname);
        restaurantService.updateRestaurant(restaurant);
    }

    @DeleteMapping("{restaurantId}/tags/{tag}")
    public void deleteTag(@PathVariable Long restaurantId, @PathVariable String tag) {
        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");
        restaurant.removeTag(tag);
        restaurantService.updateRestaurant(restaurant);
    }

}
