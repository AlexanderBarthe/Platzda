package eva.platzda.backend.logging;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LogService {

    private final LoggedEventRepository loggedEventRepository;

    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    @Autowired
    public LogService(LoggedEventRepository loggedEventRepository) {
        this.loggedEventRepository = loggedEventRepository;
    }

    public LoggedEvent getLoggedEvent(Long eventId) {
        return loggedEventRepository.getLoggedEventById(eventId);
    }

    public List<LoggedEvent> findAll() {
        return loggedEventRepository.findAll();
    }

    @Async("loggingExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addLoggedEvent(LoggedEvent loggedEvent) {
        try {
            loggedEvent.setId(null);
            loggedEventRepository.save(loggedEvent);
        } catch (Exception e) {
            log.warn("Async logging failed: {}", e.getMessage(), e);
        }
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
