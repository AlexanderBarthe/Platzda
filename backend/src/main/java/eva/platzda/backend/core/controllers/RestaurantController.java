package eva.platzda.backend.core.controllers;

import eva.platzda.backend.core.dtos.RestaurantDto;
import eva.platzda.backend.core.dtos.StringRequest;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.models.User;
import eva.platzda.backend.core.services.RestaurantService;
import eva.platzda.backend.core.services.UserService;
import eva.platzda.backend.error_handling.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Rest endpoints for managing Restaurants.
 */
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

    /**
     *
     * Returns all Restaurants.
     *
     */
    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getRestaurants() {
        return ResponseEntity.ok(restaurantService
                .findAllRestaurants().stream()
                .map(RestaurantDto::fromObject)
                .collect(Collectors.toList()));
    }

    /**
     *
     * Returns Restaurant with given Id.
     *
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getRestaurant(@PathVariable Long id) {
        Restaurant restaurant = restaurantService.findById(id);

        if(restaurant == null) {
            throw new NotFoundException("Restaurant wit id " + id + " does not exist");
        }

        return ResponseEntity.ok(RestaurantDto.fromObject(restaurant));
    }

    /**
     *
     * Returns restaurant with all searched tags
     *
     */
    @PostMapping("/search")
    public ResponseEntity<List<RestaurantDto>> getRestaurantsByTags(@RequestBody StringRequest stringRequest) {
        return ResponseEntity.ok(
                restaurantService
                        .findByTags(stringRequest.getString())
                        .stream()
                        .map(RestaurantDto::fromObject)
                        .collect(Collectors.toList()));
    }


    /**
     *
     * Creates a new Restaurant for the given owner.
     *
     * @param ownerId Id of the User who will own the restaurant
     * @param restaurant Restaurant data to create
     * @return Created Restaurant
     */
    @PostMapping("/{ownerId}")
    public ResponseEntity<RestaurantDto> createRestaurant(@PathVariable Long ownerId, @RequestBody Restaurant restaurant) {
        restaurant.setId(null);
        if(restaurant.getAddress() == null) restaurant.setAddress("");
        if(restaurant.getTimeSlotDuration() == null) restaurant.setTimeSlotDuration(6);

        User owner = userService.findById(ownerId);
        if(owner == null) {
            throw new NotFoundException("User with id " + ownerId + " does not exist");
        }
        else {
            restaurant.setOwner(owner);
        }

        Restaurant r = restaurantService.createRestaurant(restaurant);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestaurantDto.fromObject(r));
    }

    /**
     *
     * Updates the owner of a restaurant.
     *
     * @param restaurantId Id of the restaurant to update
     * @param ownerId Id of the new owner User
     * @return Updated Restaurant
     */
    @PutMapping("/{restaurantId}/owner/{ownerId}")
    public ResponseEntity<RestaurantDto> updateOwner(@PathVariable Long restaurantId, @PathVariable Long ownerId) {

        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new NotFoundException("Restaurant wit id " + restaurantId + " does not exist");

        User newOwner = userService.findById(ownerId);
        if(newOwner == null) throw new NotFoundException("User wit id " + ownerId + " does not exist");

        restaurant.setOwner(newOwner);

        Restaurant r = restaurantService.updateRestaurant(restaurant);
        return ResponseEntity.ok(RestaurantDto.fromObject(r));
    }

    /**
     *
     * Updates restaurant information.
     * Null fields in the request keep their old values.
     *
     * @param restaurant Restaurant data with updates
     * @return Updated Restaurant
     */
    @PutMapping()
    public ResponseEntity<RestaurantDto> updateRestaurant(@RequestBody Restaurant restaurant) {
        Restaurant oldRestaurant = restaurantService.findById(restaurant.getId());
        if(oldRestaurant == null) throw new NotFoundException("Restaurant wit id " + restaurant.getId() + " does not exist");

        if(restaurant.getAddress() == null) restaurant.setAddress(oldRestaurant.getAddress());
        if(restaurant.getTimeSlotDuration() == null) restaurant.setTimeSlotDuration(oldRestaurant.getTimeSlotDuration());
        if(restaurant.getTags() == null) restaurant.setTags(oldRestaurant.getTags());
        restaurant.setOwner(oldRestaurant.getOwner());

        Restaurant r = restaurantService.updateRestaurant(restaurant);
        return ResponseEntity.ok(RestaurantDto.fromObject(r));
    }

    /**
     *
     * Deletes a restaurant by Id.
     *
     * @param restaurantId Id of the restaurant to delete
     */
    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<String> deleteRestaurant(@PathVariable Long restaurantId) {
        if(restaurantService.findById(restaurantId) == null) return ResponseEntity.noContent().build();
        restaurantService.deleteRestaurantById(restaurantId);
        return ResponseEntity.ok("Restaurant deleted");
    }

    /**
     *
     * Deletes all restaurants.
     *
     */
    @DeleteMapping
    public ResponseEntity<String> deleteAllRestaurants() {
        restaurantService.deleteAllRestaurants();
        return ResponseEntity.ok("Restaurants deleted");
    }

    /**
     *
     * Adds a tag to the restaurant.
     *
     * @param restaurantId Id of the restaurant
     * @param request Tag to add
     * @return Updated Restaurant
     */
    @PutMapping("{restaurantId}/tag")
    public ResponseEntity<RestaurantDto> addTag(@PathVariable Long restaurantId, @RequestBody StringRequest request) {
        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new NotFoundException("Restaurant wit id " + restaurantId + " does not exist");

        restaurant.addTag(request.getString());
        Restaurant r = restaurantService.updateRestaurant(restaurant);

        return ResponseEntity.ok(RestaurantDto.fromObject(r));
    }

    /**
     *
     * Removes a tag from the restaurant.
     *
     * @param restaurantId Id of the restaurant
     * @param request Tag to remove
     * @return Updated Restaurant
     */
    @PutMapping("{restaurantId}/untag")
    public ResponseEntity<RestaurantDto> deleteTag(@PathVariable Long restaurantId, @RequestBody StringRequest request) {
        Restaurant restaurant = restaurantService.findById(restaurantId);
        if(restaurant == null) throw new NotFoundException("Restaurant wit id " + restaurantId + " does not exist");

        restaurant.removeTag(request.getString());
        Restaurant r = restaurantService.updateRestaurant(restaurant);

        return ResponseEntity.ok(RestaurantDto.fromObject(r));
    }

}

