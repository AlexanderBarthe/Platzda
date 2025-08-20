package eva.platzda.backend.core.repositories;

import eva.platzda.backend.core.models.OpeningHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HoursRepository extends JpaRepository<OpeningHours, Long> {

    @Query("SELECT b FROM OpeningHours b WHERE b.restaurant.id = :restaurantId")
    List<OpeningHours> findByRestaurantId(@Param("restaurantId") Long restaurantId);


    @Query("SELECT b FROm OpeningHours b WHERE b.restaurant.id = :restaurantId AND b.weekday = :weekday")
    OpeningHours findByWeekday(@Param("weekday") int weekday, @Param("restaurantId") Long restaurantId);
}
