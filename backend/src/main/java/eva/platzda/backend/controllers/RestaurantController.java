package eva.platzda.backend.controllers;

import eva.platzda.backend.dtos.RestaurantDto;
import eva.platzda.backend.dtos.TagRequest;
import eva.platzda.backend.models.Restaurant;
import eva.platzda.backend.models.User;
import eva.platzda.backend.services.RestaurantService;
import eva.platzda.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private UserService userService;
    private RestaurantService restaurantService;

    @Autowired
    public RestaurantController(UserService userService, RestaurantService restaurantService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public List<RestaurantDto> getRestaurants() {
        return restaurantService
                .findAllRestaurants().stream()
                .map(RestaurantDto::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public RestaurantDto getRestaurant(@PathVariable Long id) {
        Restaurant restaurant = restaurantService.findById(id);

        if(restaurant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");
        }

        return RestaurantDto.toDto(restaurant);
    }

    @PostMapping("/{ownerId}")
    public RestaurantDto createRestaurant(@PathVariable Long ownerId, @RequestBody Restaurant restaurant) {
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

        Restaurant r = restaurantService.createRestaurant(restaurant);
        return RestaurantDto.toDto(r);
    }

    @PutMapping("/{restaurantId}/owner/{ownerId}")
    public RestaurantDto updateOwner(@PathVariable Long restaurantId, @PathVariable Long ownerId) {

        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");

        User newOwner = userService.findById(ownerId);
        if(newOwner == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        restaurant.setOwner(newOwner);

        Restaurant r = restaurantService.updateRestaurant(restaurant);
        return RestaurantDto.toDto(r);
    }

    @PutMapping("/{restaurantId}")
    public RestaurantDto updateRestaurant(@PathVariable Long restaurantId, @RequestBody Restaurant restaurant) {
        Restaurant oldRestaurant = restaurantService.findById(restaurantId);
        if(oldRestaurant == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");

        if(restaurant.getAddress() == null) restaurant.setAddress(oldRestaurant.getAddress());
        if(restaurant.getTimeSlotDuration() == null) restaurant.setTimeSlotDuration(oldRestaurant.getTimeSlotDuration());
        if(restaurant.getTags() == null) restaurant.setTags(oldRestaurant.getTags());
        restaurant.setOwner(oldRestaurant.getOwner());

        Restaurant r = restaurantService.updateRestaurant(restaurant);
        return RestaurantDto.toDto(r);
    }

    @DeleteMapping("/{restaurantId}")
    public void deleteRestaurant(@PathVariable Long restaurantId) {
        if(restaurantService.findById(restaurantId) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");
        restaurantService.deleteRestaurantById(restaurantId);
    }

    @DeleteMapping
    public void deleteAllRestaurants() {
        restaurantService.deleteAllRestaurants();
    }

    @PutMapping("{restaurantId}/tag")
    public RestaurantDto addTag(@PathVariable Long restaurantId, @RequestBody TagRequest request) {
        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");

        restaurant.addTag(request.getTag());
        Restaurant r = restaurantService.updateRestaurant(restaurant);

        return RestaurantDto.toDto(r);
    }

    @PutMapping("{restaurantId}/untag")
    public RestaurantDto deleteTag(@PathVariable Long restaurantId, @RequestBody TagRequest request) {
        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");

        restaurant.removeTag(request.getTag());
        Restaurant r = restaurantService.updateRestaurant(restaurant);

        return RestaurantDto.toDto(r);
    }

}
