package eva.platzda.backend.core.dtos;

import eva.platzda.backend.core.models.Restaurant;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dto for wrapping up users as their ids
 */
public class RestaurantDto {
    private Long id;
    private String address;
    private Long owner;
    private Integer timeSlotDuration;
    private List<String> tags;

    /**
     * No Args Constructor
     */
    public RestaurantDto() {
    }

    /**
     * All Args Constructor
     *
     * @param id ID of the restaurant
     * @param address Address of the restaurant
     * @param ownerId ID of the restaurant owner
     * @param timeSlotDuration Duration of a reservation time slot (in minutes)
     * @param tags Tags associated with the restaurant
     */
    public RestaurantDto(Long id, String address, Long ownerId, Integer timeSlotDuration, List<String> tags) {
        this.id = id;
        this.address = address;
        this.owner = ownerId;
        this.timeSlotDuration = timeSlotDuration;
        this.tags = tags;
    }

    /**
     * Converts a Restaurant entity into a RestaurantDto.
     *
     * @param r Restaurant entity
     * @return RestaurantDto with mapped values
     */
    public static RestaurantDto fromObject(Restaurant r) {
        if (r == null) return null;

        Long ownerId = (r.getOwner() == null) ? null : r.getOwner().getId();

        // defensive copy der tags
        List<String> tags = r.getTags() == null ? Collections.emptyList() :
                r.getTags().stream().collect(Collectors.toList());

        return new RestaurantDto(
                r.getId(),
                r.getAddress(),
                ownerId,
                r.getTimeSlotDuration(),
                tags
        );
    }

    // Getter / Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getOwner() {
        return owner;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    public Integer getTimeSlotDuration() {
        return timeSlotDuration;
    }

    public void setTimeSlotDuration(Integer timeSlotDuration) {
        this.timeSlotDuration = timeSlotDuration;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}