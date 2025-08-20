package eva.platzda.backend.core.dtos;


import eva.platzda.backend.core.models.OpeningHours;

import java.time.LocalTime;

public class HoursDto {

    private Long id;
    private Integer weekday;
    private LocalTime openingTime;
    private LocalTime closingTime;

    public HoursDto(Long id, Integer weekday, LocalTime openingTime, LocalTime closingTime) {
        this.id = id;
        this.weekday = weekday;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public static HoursDto toDto(OpeningHours hours) {
        if(hours == null) return null;

        return new HoursDto(
                hours.getId(),
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
