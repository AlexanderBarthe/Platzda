package eva.platzda.backend.logging;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public Long getAvgResponseTime() {
        if(loggedEventRepository.count() == 0) return 0L;
        long adder = 0;
        long value = 0;
        for (LoggedEvent loggedEvent : findAll()) {
            adder++;
            value+= loggedEvent.getResponseTime_us();
        }
        return value / adder;
    }

    public Long getMedianResponseTime() {
        if(loggedEventRepository.count() == 0) return 0L;
        return findAll().stream().map(LoggedEvent::getResponseTime_us).sorted().toList().get(findAll().size()/2);
    }

    public Long getMaxResponseTime() {
        if(loggedEventRepository.count() == 0) return 0L;
        return findAll().stream().map(LoggedEvent::getResponseTime_us).max(Long::compareTo).get();
    }

    public String getEndpointUssage(String endpoint) {
        List<LoggedEvent> eventsOnEndpoint = findAll().stream().filter(log -> log.getEndpoint().equals(endpoint)).toList();

        int requestsPastMinute = 0;
        int requestsPastHour = 0;
        int requestsPastDay = 0;

        LocalDateTime now = LocalDateTime.now();

        for (LoggedEvent loggedEvent : eventsOnEndpoint) {

            LocalDateTime eventTime = loggedEvent.getTimestamp();

            if(now.minusMinutes(1).isBefore(eventTime)) requestsPastMinute++;
            if(now.minusHours(1).isBefore(eventTime)) requestsPastHour++;
            if(now.minusDays(1).isBefore(eventTime)) requestsPastDay++;
        }

        String answer = "Requests past minute: " + requestsPastMinute + "\n"
                + "Requests past hour: " + requestsPastHour + "\n"
                + "Requests past day: " + requestsPastDay;

        return answer;

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
