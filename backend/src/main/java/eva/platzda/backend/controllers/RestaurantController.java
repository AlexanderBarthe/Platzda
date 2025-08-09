package eva.platzda.backend.controllers;

import eva.platzda.backend.dtos.RestaurantDto;
import eva.platzda.backend.dtos.TagRequest;
import eva.platzda.backend.error_handling.NotFoundException;
import eva.platzda.backend.models.Restaurant;
import eva.platzda.backend.models.User;
import eva.platzda.backend.services.RestaurantService;
import eva.platzda.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<RestaurantDto>> getRestaurants() {
        return ResponseEntity.ok(restaurantService
                .findAllRestaurants().stream()
                .map(RestaurantDto::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getRestaurant(@PathVariable Long id) {
        Restaurant restaurant = restaurantService.findById(id);

        if(restaurant == null) {
            throw new NotFoundException("Restaurant wit id " + id + " does not exist");
        }

        return ResponseEntity.ok(RestaurantDto.toDto(restaurant));
    }

    @PostMapping("/{ownerId}")
    public ResponseEntity<RestaurantDto> createRestaurant(@PathVariable Long ownerId, @RequestBody Restaurant restaurant) {
        restaurant.setId(null);
        if(restaurant.getAddress() == null) restaurant.setAddress("");
        if(restaurant.getTimeSlotDuration() == null) restaurant.setTimeSlotDuration(90);


        User owner = userService.findById(ownerId);
        if(owner == null) {
            throw new NotFoundException("User wit id " + ownerId + " does not exist");
        }
        else {
            restaurant.setOwner(owner);
        }

        Restaurant r = restaurantService.createRestaurant(restaurant);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestaurantDto.toDto(r));
    }

    @PutMapping("/{restaurantId}/owner/{ownerId}")
    public ResponseEntity<RestaurantDto> updateOwner(@PathVariable Long restaurantId, @PathVariable Long ownerId) {

        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new NotFoundException("Restaurant wit id " + restaurantId + " does not exist");

        User newOwner = userService.findById(ownerId);
        if(newOwner == null) throw new NotFoundException("User wit id " + ownerId + " does not exist");

        restaurant.setOwner(newOwner);

        Restaurant r = restaurantService.updateRestaurant(restaurant);
        return ResponseEntity.ok(RestaurantDto.toDto(r));
    }

    @PutMapping()
    public ResponseEntity<RestaurantDto> updateRestaurant(@RequestBody Restaurant restaurant) {
        Restaurant oldRestaurant = restaurantService.findById(restaurant.getId());
        if(oldRestaurant == null) throw new NotFoundException("Restaurant wit id " + restaurant.getId() + " does not exist");

        if(restaurant.getAddress() == null) restaurant.setAddress(oldRestaurant.getAddress());
        if(restaurant.getTimeSlotDuration() == null) restaurant.setTimeSlotDuration(oldRestaurant.getTimeSlotDuration());
        if(restaurant.getTags() == null) restaurant.setTags(oldRestaurant.getTags());
        restaurant.setOwner(oldRestaurant.getOwner());

        Restaurant r = restaurantService.updateRestaurant(restaurant);
        return ResponseEntity.ok(RestaurantDto.toDto(r));
    }

    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long restaurantId) {
        if(restaurantService.findById(restaurantId) == null) return ResponseEntity.noContent().build();
        restaurantService.deleteRestaurantById(restaurantId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllRestaurants() {
        restaurantService.deleteAllRestaurants();
        return ResponseEntity.ok().build();
    }

    @PutMapping("{restaurantId}/tag")
    public ResponseEntity<RestaurantDto> addTag(@PathVariable Long restaurantId, @RequestBody TagRequest request) {
        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new NotFoundException("Restaurant wit id " + restaurantId + " does not exist");

        restaurant.addTag(request.getTag());
        Restaurant r = restaurantService.updateRestaurant(restaurant);

        return ResponseEntity.ok(RestaurantDto.toDto(r));
    }

    @PutMapping("{restaurantId}/untag")
    public ResponseEntity<RestaurantDto> deleteTag(@PathVariable Long restaurantId, @RequestBody TagRequest request) {
        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new NotFoundException("Restaurant wit id " + restaurantId + " does not exist");

        restaurant.removeTag(request.getTag());
        Restaurant r = restaurantService.updateRestaurant(restaurant);

        return ResponseEntity.ok(RestaurantDto.toDto(r));
    }

}
