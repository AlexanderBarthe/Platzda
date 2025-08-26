package eva.platzda.backend.core.dtos;

import eva.platzda.backend.core.models.RestaurantTable;

/**
 * Dto for wrapping Tables as their ids
 */
public class TableDto {

    private Long id;
    private final Long restaurantId;
    private int size;

    /**
     * All Args Constructor.
     *
     * @param id ID of the table
     * @param restaurant ID of the restaurant the table belongs to
     * @param size Seating capacity of the table
     */
    public TableDto(Long id, Long restaurant, int size) {
        this.id = id;
        this.restaurantId = restaurant;
        this.size = size;
    }

    /**
     * Converts a RestaurantTable entity into a TableDto.
     *
     * @param table RestaurantTable entity
     * @return TableDto with mapped values
     */
    public static TableDto fromObject(RestaurantTable table) {
        if(table==null) return null;

        Long restaurantId = (table.getRestaurant() == null) ? null : table.getRestaurant().getId();

        return new TableDto(
                table.getId(),
                restaurantId,
                table.getSize()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }
}
