package eva.platzda.backend.logging;

import eva.platzda.backend.core.dtos.StringRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<LoggedEvent>> findAll() {
        return ResponseEntity.ok(logService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoggedEvent> getLoggedEvent(@PathVariable Long id) {
        return ResponseEntity.ok(logService.getLoggedEvent(id));
    }

    @GetMapping("avg-time")
    public ResponseEntity<Long> getAverageResponseTime() {
        return ResponseEntity.ok(logService.getAvgResponseTime());
    }

    @GetMapping("med-time")
    public ResponseEntity<Long> getMedianResponseTime() {
        return ResponseEntity.ok(logService.getMedianResponseTime());
    }

    @GetMapping("max-time")
    public ResponseEntity<Long> getMaxResponseTime() {
        return ResponseEntity.ok(logService.getMaxResponseTime());
    }

    @GetMapping("/success")
    public ResponseEntity<List<LoggedEvent>> getSuccessfulEvents() {
        return ResponseEntity.ok(logService.findAll().stream().filter(loggedEvent -> loggedEvent.getStatusCode() >= 200 && loggedEvent.getStatusCode() <= 299).toList());
    }

    @GetMapping("/client-errors")
    public ResponseEntity<List<LoggedEvent>> getClientErrorEvents() {
        return ResponseEntity.ok(logService.findAll().stream().filter(loggedEvent -> loggedEvent.getStatusCode() >= 400 && loggedEvent.getStatusCode() <= 499).toList());
    }

    @GetMapping("/server-errors")
    public ResponseEntity<List<LoggedEvent>> getServerErrorEvents() {
        return ResponseEntity.ok(logService.findAll().stream().filter(loggedEvent -> loggedEvent.getStatusCode() >= 500 && loggedEvent.getStatusCode() <= 599).toList());
    }

    @PostMapping("/endpoint-ussage")
    public ResponseEntity<String> getEndpointUsage(@RequestBody StringRequest endpoint) {
        return ResponseEntity.ok(logService.getEndpointUssage(endpoint.getString()));
    }


    @DeleteMapping
    public ResponseEntity<String> deleteAll() {
        logService.deleteAllLoggedEvents();
        return ResponseEntity.ok("All logs deleted");
    }

}
