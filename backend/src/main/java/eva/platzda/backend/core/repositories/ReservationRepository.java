package eva.platzda.backend.core.repositories;

import eva.platzda.backend.core.models.Reservation;
import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.models.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
SELECT r FROM Reservation r
WHERE r.restaurantTable = :table
AND r.startTime < :dayEnd
AND r.endTime > :dayStart
""")
    List<Reservation> findAllForTableAndDay(@Param("table")RestaurantTable table,
                                            @Param("dayStart")LocalDateTime dayStart,
                                            @Param("dayEnd") LocalDateTime dayEnd);

    @Query("""
SELECT COUNT(r) > 0 
FROM Reservation r 
WHERE r.user.id = :userId
AND r.startTime >= :dayStart
AND r.endTime <= :dayEnd    
""")
    boolean existsReservationForUserOnDay(@Param("userId") Long userId,
                                          @Param("dayStart") LocalDateTime dayStart,
                                          @Param("dayEnd") LocalDateTime dayEnd);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
SELECT r FROM Reservation r 
WHERE r.restaurantTable = :table
AND r.startTime < :endTIme
ANd r.endTime > :startTime
""")
    List<Reservation> findOverlapsForUpdate(@Param("table") RestaurantTable table,
                                            @Param("startTime") LocalDateTime start,
                                            @Param("endTime") LocalDateTime end);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.restaurantTable.restaurant.id = :restaurantId
             AND r.startTime >= :dayStart
             AND r.endTime   <= :dayEnd
           ORDER BY r.startTime
           """)
    List<Reservation> findReservationsForRestaurant(@Param("restaurantId") Long restaurantId,
                                                    @Param("dayStart") LocalDateTime dayStart,
                                                    @Param("dayEnd") LocalDateTime dayEnd);

    @Query("""
SELECT u FROM Reservation u WHERE u.user = :user ORDER BY u.startTime
""")
    List<Reservation> findReservationForUser(@Param("user") User user);
}
