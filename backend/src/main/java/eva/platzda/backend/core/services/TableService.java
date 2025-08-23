package eva.platzda.backend.core.services;

import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.repositories.TableRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableService {

    private final TableRepository tableRepository;

    public TableService(TableRepository tableRepository) {this.tableRepository = tableRepository;}

    @Transactional
    public List<RestaurantTable> findAllTablesAllRestaurants() {return tableRepository.findAll();}

    @Transactional
    public List<RestaurantTable> findAllTablesRestaurant(Long restaurantId) {return tableRepository.findByRestaurantId(restaurantId);}

    @Transactional
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
