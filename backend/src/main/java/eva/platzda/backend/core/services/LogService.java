package eva.platzda.backend.core.services;

import eva.platzda.backend.core.models.LoggedEvent;
import eva.platzda.backend.core.repositories.LoggedEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

    private final LoggedEventRepository loggedEventRepository;

    @Autowired
    public LogService(LoggedEventRepository loggedEventRepository) {
        this.loggedEventRepository = loggedEventRepository;
    }

    public List<LoggedEvent> findAll() {
        return loggedEventRepository.findAll();
    }

    @Transactional
    public void addLoggedEvent(LoggedEvent loggedEvent) {
        loggedEvent.setId(null);
        loggedEventRepository.save(loggedEvent);
    }

    @Transactional
    public void editLoggedEvent(LoggedEvent loggedEvent) {
        loggedEventRepository.save(loggedEvent);
    }

    @Transactional
    public void deleteLoggedEvent(LoggedEvent loggedEvent) {
        loggedEventRepository.delete(loggedEvent);
    }

    @Transactional
    public void deleteAllLoggedEvents() {
        loggedEventRepository.deleteAll();
    }

}
