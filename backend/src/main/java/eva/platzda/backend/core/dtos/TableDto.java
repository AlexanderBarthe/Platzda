package eva.platzda.backend.core.dtos;

import eva.platzda.backend.core.models.RestaurantTable;

public class TableDto {

    private Long id;
    private int size;

    public TableDto(Long id, int size) {
        this.id = id;
        this.size = size;
    }

    public static TableDto toDto(RestaurantTable table) {
        if(table==null) return null;

        return new TableDto(
                table.getId(),
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
}
