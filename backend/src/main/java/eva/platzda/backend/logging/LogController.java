package eva.platzda.backend.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public List<LoggedEvent> findAll() {
        return logService.findAll();
    }

    @GetMapping("/{id}")
    public LoggedEvent getLoggedEvent(@PathVariable Long id) {
        return logService.getLoggedEvent(id);
    }

    @GetMapping("/success")
    public List<LoggedEvent> getSuccessfulEvents() {
        return logService.findAll().stream().filter(loggedEvent -> loggedEvent.getStatusCode() >= 200 && loggedEvent.getStatusCode() <= 299).toList();
    }

    @GetMapping("/client-errors")
    public List<LoggedEvent> getClientErrorEvents() {
        return logService.findAll().stream().filter(loggedEvent -> loggedEvent.getStatusCode() >= 400 && loggedEvent.getStatusCode() <= 499).toList();
    }

    @GetMapping("/server-errors")
    public List<LoggedEvent> getServerErrorEvents() {
        return logService.findAll().stream().filter(loggedEvent -> loggedEvent.getStatusCode() >= 500).toList();
    }

}
