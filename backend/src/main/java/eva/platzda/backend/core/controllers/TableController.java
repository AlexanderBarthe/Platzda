package eva.platzda.backend.core.controllers;


import eva.platzda.backend.core.dtos.TableDto;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.services.RestaurantService;
import eva.platzda.backend.core.services.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/get/{restaurantId}")
    public ResponseEntity<List<TableDto>> getTables(@PathVariable Long restaurantId){
        List<RestaurantTable> tables= tableService.findAllTablesRestaurant(restaurantId);
        List<TableDto> tableDtos = new ArrayList<>();
        for (RestaurantTable table:tables){
            tableDtos.add(TableDto.toDto(table));
        }
        return ResponseEntity.ok(tableDtos);
    }

    @PostMapping("/create/{restaurantId}")
    public ResponseEntity<TableDto> createTable(@PathVariable Long restaurantId, @RequestBody RestaurantTable restaurantTable){
        Restaurant r = restaurantService.findById(restaurantId);

        restaurantTable.setRestaurant(r);

        RestaurantTable saved = tableService.createTable(restaurantTable);
        return ResponseEntity.status(HttpStatus.CREATED).body(TableDto.toDto(saved));
    }

    @PutMapping("update/{tableId}")
    public ResponseEntity<TableDto> updateTable(@PathVariable Long tableId, @RequestBody RestaurantTable table) {
        RestaurantTable oldTable = tableService.findById(tableId);

        oldTable.setSize(table.getSize());
        RestaurantTable saved = tableService.updateTable(oldTable);

        return ResponseEntity.ok(TableDto.toDto(saved));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllTables(){
        tableService.deleteAll();
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{tableId}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long tableId) {
        if (tableService.findById(tableId) == null) return ResponseEntity.noContent().build();
        tableService.deleteTable(tableId);
        return ResponseEntity.ok().build();
    }
}
