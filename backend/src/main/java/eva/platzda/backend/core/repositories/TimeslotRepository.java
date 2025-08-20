package eva.platzda.backend.core.repositories;

import eva.platzda.backend.core.models.Timeslot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeslotRepository extends JpaRepository<Timeslot, Long> {

    @Query("SELECT b FROM Timeslot b WHERE b.user = null")
    List<Timeslot> findFreeTimeslots();
}
