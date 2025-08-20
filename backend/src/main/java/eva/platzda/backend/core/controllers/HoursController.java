package eva.platzda.backend.core.controllers;


import eva.platzda.backend.core.dtos.HoursDto;
import eva.platzda.backend.core.models.OpeningHours;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.repositories.HoursRepository;
import eva.platzda.backend.core.services.HoursService;
import eva.platzda.backend.core.services.RestaurantService;
import eva.platzda.backend.error_handling.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/hours")
public class HoursController {

    private RestaurantService restaurantService;
    private HoursService hoursService;
    private final HoursRepository hoursRepository;

    @Autowired
    public HoursController(RestaurantService restaurantService, HoursService hoursService,HoursRepository hoursRepository) {
        this.restaurantService = restaurantService;
        this.hoursService = hoursService;
        this.hoursRepository = hoursRepository;
    }

    /**
     *
     * Returns all Opening Hours to a Restaurant with given (restaurant) Id.
     *
     */
    @GetMapping("/get/{restaurantId}")
    public ResponseEntity<List<HoursDto>> getHours(@PathVariable Long restaurantId) {
        Restaurant restaurant = restaurantService.findById(restaurantId);

        if(restaurant == null) {
            throw new NotFoundException("Restaurant wit id " + restaurantId + " does not exist");
        }

        List<OpeningHours> hours = hoursRepository.findByRestaurantId(restaurantId);
        List<HoursDto> hoursDtos = new ArrayList<>();
        for(OpeningHours OpeningsHours: hours){
            hoursDtos.add(HoursDto.toDto((OpeningHours) hours));
        }
        return ResponseEntity.ok(hoursDtos);

    }

    @PostMapping("/create/{restaurantId}")
    public ResponseEntity<HoursDto> createHours(@PathVariable Long restaurantId, @RequestBody OpeningHours hours) {
        Restaurant r = restaurantService.findById(restaurantId);

        hours.setRestaurant(r);
        OpeningHours saved = hoursService.createOpeningHours(hours);

        return ResponseEntity.status(HttpStatus.CREATED).body(HoursDto.toDto(saved));
    }

    @PutMapping("/update/{restaurantId}")
    public ResponseEntity<HoursDto> updateHours(@PathVariable Long restaurantId, @RequestBody OpeningHours newHours) {

        OpeningHours oldHours = hoursRepository.findByWeekday(newHours.getWeekday(), restaurantId);

        oldHours.setOpeningTime(newHours.getOpeningTime());
        oldHours.setClosingTime(newHours.getClosingTime());

        OpeningHours saved = hoursService.updateOpeningHours(oldHours);

        return ResponseEntity.ok(HoursDto.toDto(saved));

    }

    @DeleteMapping("/{hoursId}")
    public ResponseEntity<Void> deleteOpeningHours(@PathVariable Long hoursId) {
        if(hoursService.findById(hoursId) == null) return ResponseEntity.noContent().build();
        hoursService.deleteOpeningHoursById(hoursId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllOpeningHours(){
        hoursService.deleteAllOpeningHours();
        return ResponseEntity.ok().build();
    }



}
