package eva.platzda.backend.logging;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoggedEventRepository extends JpaRepository<LoggedEvent, Integer> {
    LoggedEvent getLoggedEventById(Long id);
}
