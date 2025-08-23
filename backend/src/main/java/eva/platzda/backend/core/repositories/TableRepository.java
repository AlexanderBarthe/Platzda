package eva.platzda.backend.core.repositories;

import eva.platzda.backend.core.models.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {

    @Query("SELECT b FROM RestaurantTable b WHERE b.restaurant.id = :restaurantId")
    List<RestaurantTable> findAllTablesRestaurant(@Param("restaurantId") Long restaurantId);

    List<RestaurantTable> findByRestaurantId(Long restaurantId);

    @Modifying
    @Query("DELETE FROM RestaurantTable t WHERE t.restaurant.id = :restaurantId")
    void deleteByRestaurantId(@Param("restaurantId") Long restaurantId);
}
