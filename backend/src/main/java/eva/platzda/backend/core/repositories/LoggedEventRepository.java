package eva.platzda.backend.core.repositories;

import eva.platzda.backend.core.models.LoggedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoggedEventRepository extends JpaRepository<LoggedEvent, Integer> {
}
