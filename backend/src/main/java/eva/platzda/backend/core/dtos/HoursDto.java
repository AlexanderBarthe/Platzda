package eva.platzda.backend.core.dtos;


import eva.platzda.backend.core.models.OpeningHours;

import java.time.LocalTime;

/**
 * Dto for wrapping up reservations as their ids
 */
public class HoursDto {

    private Long id;
    private final Long restaurantId;
    private final Integer weekday;
    private LocalTime openingTime;
    private LocalTime closingTime;

    /**
     * All Args Constructor
     *
     * @param id ID of the opening hours entry
     * @param restaurant ID of the restaurant
     * @param weekday Weekday of the opening hours (1 = Monday, 7 = Sunday)
     * @param openingTime Opening time
     * @param closingTime Closing time
     */
    public HoursDto(Long id, Long restaurant, Integer weekday, LocalTime openingTime, LocalTime closingTime) {
        this.id = id;
        this.restaurantId = restaurant;
        this.weekday = weekday;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    /**
     * Converts an OpeningHours entity into an HoursDto.
     *
     * @param hours OpeningHours entity
     * @return HoursDto with mapped values
     */
    public static HoursDto fromObject(OpeningHours hours) {
        if(hours == null) return null;


        return new HoursDto(
                hours.getId(),
                (hours.getRestaurant() != null ? hours.getRestaurant().getId() : null),
                hours.getWeekday(),
                hours.getOpeningTime(),
                hours.getClosingTime()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getWeekday() {
        return weekday;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }
}
