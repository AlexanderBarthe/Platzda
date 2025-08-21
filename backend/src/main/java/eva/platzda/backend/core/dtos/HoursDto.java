package eva.platzda.backend.core.dtos;


import eva.platzda.backend.core.models.OpeningHours;

import java.time.LocalTime;

public class HoursDto {

    private Long id;
    private Long restaurantId;
    private Integer weekday;
    private LocalTime openingTime;
    private LocalTime closingTime;

    public HoursDto(Long id, Long restaurant, Integer weekday, LocalTime openingTime, LocalTime closingTime) {
        this.id = id;
        this.restaurantId = restaurant;
        this.weekday = weekday;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public static HoursDto toDto(OpeningHours hours) {
        if(hours == null) return null;

        Long restaurantId = (hours.getRestaurant() == null) ? null : hours.getRestaurant().getId();

        return new HoursDto(
                hours.getId(),
                restaurantId,
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

    public void setWeekday(Integer weekday) {
        this.weekday = weekday;
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
}
