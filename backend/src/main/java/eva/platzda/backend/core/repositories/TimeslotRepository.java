package eva.platzda.backend.core.repositories;

import eva.platzda.backend.core.models.RestaurantTable;
import eva.platzda.backend.core.models.Timeslot;
import eva.platzda.backend.core.models.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeslotRepository extends JpaRepository<Timeslot, Long> {

    @Query("SELECT b FROM Timeslot b WHERE b.user = null")
    List<Timeslot> findFreeTimeslots();

    @Query("""
            SELECT t from Timeslot t 
            WHERE t.startTime >= :start
            AND t.endTime <= :end
            AND t.user = :user
""")
    List<Timeslot> findTimeslotsForReservation(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("user") User user);

    @Query("""
           SELECT t FROM Timeslot t
           WHERE t.table.restaurant.id = :restaurantId
             AND t.startTime >= :dayStart
             AND t.endTime   <= :dayEnd
           ORDER BY t.table.id, t.startTime
           """)
    List<Timeslot> findAllForRestaurantAndDay(@Param("restaurantId") Long restaurantId,
                                              @Param("dayStart") LocalDateTime dayStart,
                                              @Param("dayEnd")   LocalDateTime dayEnd);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT t FROM Timeslot t
           WHERE t.table = :table
             AND t.startTime >= :start
             AND t.endTime   <= :end
           ORDER BY t.startTime
           """)
    List<Timeslot> findSlotsForUpdate(@Param("table") RestaurantTable table,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("""
SELECT t FROM Timeslot t
WHERE t.user = :user
""")
    List<Timeslot> findSlotsUser(@Param("user") User user);
}
