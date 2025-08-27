package eva.platzda.backend.core.controllers;


import eva.platzda.backend.core.dtos.HoursDto;
import eva.platzda.backend.core.models.OpeningHours;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.services.HoursService;
import eva.platzda.backend.core.services.RestaurantService;
import eva.platzda.backend.error_handling.BadRequestBodyException;
import eva.platzda.backend.error_handling.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST endpoints for managing opening hours of restaurants.
 */
@RestController
@RequestMapping("/hours")
public class HoursController {

    private RestaurantService restaurantService;
    private HoursService hoursService;

    @Autowired
    public HoursController(RestaurantService restaurantService, HoursService hoursService) {
        this.restaurantService = restaurantService;
        this.hoursService = hoursService;
    }

    /**
     * Returns all opening hours of a restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @return List of opening hours as DTOs
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<HoursDto>> getHours(@PathVariable Long restaurantId) {
        Restaurant restaurant = restaurantService.findById(restaurantId);

        if(restaurant == null) {
            throw new NotFoundException("Restaurant with id " + restaurantId + " does not exist");
        }

        List<OpeningHours> hours = hoursService.findByRestaurantId(restaurantId);
        List<HoursDto> hoursDtos = new ArrayList<>();
        for(OpeningHours h: hours){
            hoursDtos.add(HoursDto.fromObject(h));
        }
        return ResponseEntity.ok(hoursDtos);

    }


    /**
     * Creates new opening hours for a restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @param newHours Opening hours details
     * @return Created opening hours as DTO
     * @throws BadRequestBodyException if weekday or times are invalid
     */
    @PostMapping("/{restaurantId}")
    public ResponseEntity<HoursDto> createHours(@PathVariable Long restaurantId, @RequestBody HoursDto newHours) {

        if(newHours == null) throw new BadRequestBodyException("New Hours cannot be null");
        if(newHours.getWeekday() == null || newHours.getWeekday() < 1 || newHours.getWeekday() > 7) throw  new BadRequestBodyException("Weekday must be between 1 and 7");
        if(newHours.getOpeningTime() == null || newHours.getClosingTime() == null) throw  new BadRequestBodyException("Opening and closing time must be specified");
        if(newHours.getClosingTime().isBefore(newHours.getOpeningTime())) throw  new BadRequestBodyException("Closing time cannot be before Opening time");

        Restaurant restaurant = restaurantService.findById(restaurantId);

        if(restaurant == null) {
            throw new NotFoundException("Restaurant with id " + restaurantId + " does not exist");
        }

        OpeningHours hours = new OpeningHours();

        hours.setWeekday(newHours.getWeekday());
        hours.setOpeningTime(newHours.getOpeningTime());
        hours.setClosingTime(newHours.getClosingTime());

        hours.setRestaurant(restaurant);
        OpeningHours saved = hoursService.createOpeningHours(hours);

        return ResponseEntity.status(HttpStatus.CREATED).body(HoursDto.fromObject(saved));
    }


    /**
     * Updates existing opening hours.
     *
     * @param newHours Updated opening hours details
     * @return Updated opening hours as DTO
     * @throws BadRequestBodyException if closing time is before opening time
     */
    @PutMapping()
    public ResponseEntity<HoursDto> updateHours(@RequestBody HoursDto newHours) {

        if(newHours == null) throw new BadRequestBodyException("New Hours cannot be null");

        OpeningHours oldHours = hoursService.findById(newHours.getId());

        if(oldHours == null) throw new NotFoundException("OpeningHours with id " + newHours.getId() + " do not exist");

        if(newHours.getOpeningTime() != null) oldHours.setOpeningTime(newHours.getOpeningTime());
        if(newHours.getClosingTime() != null) oldHours.setClosingTime(newHours.getClosingTime());

        if(oldHours.getClosingTime().isBefore(oldHours.getOpeningTime())) throw  new BadRequestBodyException("Closing time cannot be before Opening time");

        OpeningHours saved = hoursService.updateOpeningHours(oldHours);

        return ResponseEntity.ok(HoursDto.fromObject(saved));

    }

    /**
     * Deletes opening hours by their ID.
     *
     * @param hoursId ID of the opening hours
     * @return Confirmation message or 204 if not found
     */
    @DeleteMapping("/single/{hoursId}")
    public ResponseEntity<String> deleteOpeningHours(@PathVariable Long hoursId) {
        if(hoursService.findById(hoursId) == null) return ResponseEntity.noContent().build();
        hoursService.deleteOpeningHoursById(hoursId);
        return ResponseEntity.ok("Opening hours deleted");
    }

    /**
     * Deletes all opening hours of a restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @return Confirmation message or 204 if restaurant not found
     */
    @DeleteMapping("/restaurant/{restaurantId}")
    public ResponseEntity<String> deleteByRestaurant(@PathVariable Long restaurantId) {
        if(restaurantService.findById(restaurantId) == null) return ResponseEntity.noContent().build();
        hoursService.deleteOpeningHoursOfRestaurant(restaurantId);
        return ResponseEntity.ok("Opening hours deleted");
    }

    /**
     * Deletes all opening hours in the system.
     *
     * @return Confirmation message
     */
    @DeleteMapping
    public ResponseEntity<String> deleteAllOpeningHours(){
        hoursService.deleteAllOpeningHours();
        return ResponseEntity.ok("Opening hours deleted");
    }

}
