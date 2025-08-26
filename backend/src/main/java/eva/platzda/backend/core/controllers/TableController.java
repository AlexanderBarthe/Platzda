package eva.platzda.backend.core.controllers;


import eva.platzda.backend.core.dtos.TableDto;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.services.RestaurantService;
import eva.platzda.backend.core.services.TableService;
import eva.platzda.backend.error_handling.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST endpoints for managing tables of restaurants.
 */
@RestController
@RequestMapping("/table")
public class TableController {

    private RestaurantService restaurantService;
    private TableService tableService;

    @Autowired
    public TableController(RestaurantService restaurantService, TableService tableService) {
        this.restaurantService = restaurantService;
        this.tableService = tableService;
    }

    /**
     * Returns all tables of a restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @return List of tables as DTOs
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<TableDto>> getTables(@PathVariable Long restaurantId){
        List<RestaurantTable> tables = tableService.findAllTablesRestaurant(restaurantId);
        List<TableDto> tableDtos = new ArrayList<>();
        for (RestaurantTable table:tables){
            tableDtos.add(TableDto.fromObject(table));
        }
        return ResponseEntity.ok(tableDtos);
    }

    /**
     * Creates a new table for a restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @param creationRequestObject Table details for creation
     * @return Created table as DTO
     */
    @PostMapping("/{restaurantId}")
    public ResponseEntity<TableDto> createTable(@PathVariable Long restaurantId, @RequestBody TableDto creationRequestObject) {
        Restaurant restaurant = restaurantService.findById(restaurantId);

        if (restaurant == null) throw new NotFoundException("Restaurant with id" + restaurantId + " does not exist");

        RestaurantTable restaurantTable = new RestaurantTable();

        restaurantTable.setRestaurant(restaurant);
        restaurantTable.setSize(creationRequestObject.getSize());

        RestaurantTable saved = tableService.createTable(restaurantTable);
        return ResponseEntity.status(HttpStatus.CREATED).body(TableDto.fromObject(saved));
    }


    /**
     * Updates an existing table.
     *
     * @param tableId ID of the table to update
     * @param table Updated table details
     * @return Updated table as DTO
     */
    @PutMapping("/{tableId}")
    public ResponseEntity<TableDto> updateTable(@PathVariable Long tableId, @RequestBody TableDto table) {
        RestaurantTable oldTable = tableService.findById(tableId);

        if(oldTable == null) throw new NotFoundException("Table with id" + tableId + " does not exist");

        oldTable.setSize(table.getSize());
        RestaurantTable saved = tableService.updateTable(oldTable);

        return ResponseEntity.ok(TableDto.fromObject(saved));
    }

    /**
     * Deletes all tables in the system.
     *
     * @return Confirmation message
     */
    @DeleteMapping
    public ResponseEntity<String> deleteAllTables(){
        tableService.deleteAll();
        return ResponseEntity.ok("Tables deleted");
    }



    /**
     * Deletes a single table by ID.
     *
     * @param tableId ID of the table to delete
     * @return Confirmation message or 204 if not found
     */
    @DeleteMapping("/single/{tableId}")
    public ResponseEntity<String> deleteTable(@PathVariable Long tableId) {
        if (tableService.findById(tableId) == null) return ResponseEntity.noContent().build();
        tableService.deleteTable(tableId);
        return ResponseEntity.ok("Table deleted");
    }

    /**
     * Deletes all tables of a restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @return Confirmation message or 204 if restaurant not found
     */
    @DeleteMapping("/restaurant/{restaurantId}")
    public ResponseEntity<String> deleteTableByRestaurant(@PathVariable Long restaurantId) {
        if (restaurantService.findById(restaurantId) == null) return ResponseEntity.noContent().build();
        tableService.deleteTablesOfRestaurant(restaurantId);
        return ResponseEntity.ok("Tables deleted");
    }
}
