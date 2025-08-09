package eva.platzda.backend.core.repositories;

import eva.platzda.backend.core.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
