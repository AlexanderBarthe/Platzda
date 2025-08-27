package eva.platzda.backend.core.services;

import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.repositories.TableRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing restaurant tables.
 *
 * Provides methods to create, read, update, and delete tables
 * for individual restaurants or all restaurants.
 */
@Service
public class TableService {

    private final TableRepository tableRepository;
    private final TimeslotGenerationService timeslotGenerationService;

    private final EntityManager em;
    /**
     * All Args Constructor
     * @param tableRepository
     */
    public TableService(TableRepository tableRepository,
                        TimeslotGenerationService timeslotGenerationService,
                        EntityManager em) {
        this.tableRepository = tableRepository;
        this.timeslotGenerationService = timeslotGenerationService;
        this.em = em;
    }

    /**
     * Returns all tables across all restaurants.
     *
     * @return List of all RestaurantTable entities
     */
    public List<RestaurantTable> findAllTablesAllRestaurants() {return tableRepository.findAll();}

    /**
     * Returns all tables for a specific restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @return List of RestaurantTable entities
     */
    public List<RestaurantTable> findAllTablesRestaurant(Long restaurantId) {return tableRepository.findByRestaurantId(restaurantId);}

    /**
     * Finds a table by its ID.
     *
     * @param id ID of the table
     * @return RestaurantTable entity
     * @throws RuntimeException if no table is found with the given ID
     */
    public RestaurantTable findById(Long id) {return tableRepository.findById(id).orElse(null);}


    /**
     * Creates a new table.
     *
     * @param table RestaurantTable entity to create
     * @return The created RestaurantTable entity
     */
    @Transactional
    public RestaurantTable createTable(RestaurantTable table) {
        table.setId(null);
        RestaurantTable created = tableRepository.save(table);
        for (int i = 0; i <= timeslotGenerationService.getPregeneratedWeeks()*7; i++){
            timeslotGenerationService.connectTimeslotsTable(LocalDate.now().plusDays(i), created);
        }
        return created;
    }

    /**
     * Updates an existing table.
     *
     * @param table RestaurantTable entity to update
     * @return The updated RestaurantTable entity
     */
    @Transactional
    public RestaurantTable updateTable(RestaurantTable table) {return tableRepository.save(table);}

    /**
     * Deletes a table by its ID.
     *
     * @param id ID of the table to delete
     */
    @Transactional
    public void deleteTable(Long id) {tableRepository.deleteById(id);}


    /**
     * Deletes all tables belonging to a specific restaurant.
     *
     * @param restaurantId ID of the restaurant
     */
    @Transactional
    public void deleteTablesOfRestaurant(Long restaurantId) {
        tableRepository.deleteByRestaurantId(restaurantId);
    }


    /**
     * Deletes all tables in the system.
     */
    @Transactional
    public void deleteAll() {
        tableRepository.deleteAll();
        em.createNativeQuery("ALTER TABLE restaurant_table ALTER COLUMN id RESTART WITH 1").executeUpdate();
    }
}
