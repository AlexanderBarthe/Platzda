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

}
