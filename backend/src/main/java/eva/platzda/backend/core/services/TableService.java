package eva.platzda.backend.core.services;

import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.repositories.TableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TableService {

    private final TableRepository tableRepository;

    public TableService(TableRepository tableRepository) {this.tableRepository = tableRepository;}

    public List<RestaurantTable> findAllTablesAllRestaurants() {return tableRepository.findAll();}

    public List<RestaurantTable> findAllTablesRestaurant(Long restaurantId) {return tableRepository.findByRestaurantId(restaurantId);}

    public RestaurantTable findById(Long id) {return tableRepository.findById(id).orElseThrow(() -> new RuntimeException("Table not found with id " + id));}

    @Transactional
    public RestaurantTable createTable(RestaurantTable table) {
        table.setId(null);
        return tableRepository.save(table);
    }

    @Transactional
    public RestaurantTable updateTable(RestaurantTable table) {return tableRepository.save(table);}

    @Transactional
    public void deleteTable(Long id) {tableRepository.deleteById(id);}

    @Transactional
    public void deleteTablesOfRestaurant(Long restaurantId) {
        tableRepository.deleteByRestaurantId(restaurantId);
    }

    @Transactional
    public void deleteAll() {tableRepository.deleteAll();}
}
