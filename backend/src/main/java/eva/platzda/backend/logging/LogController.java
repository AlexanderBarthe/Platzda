package eva.platzda.backend.logging;

import eva.platzda.backend.core.dtos.StringRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * Rest endpoint for logs and stats retrieval and management.
 *
 */
@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     *
     * Returns all log entries
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<List<LoggedEvent>> findAll() {
        return ResponseEntity.ok(logService.findAll());
    }

    /**
     *
     * Returns a log of given id.
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoggedEvent> getLoggedEvent(@PathVariable Long id) {
        return ResponseEntity.ok(logService.getLoggedEvent(id));
    }

    /**
     *
     * Returns average processing time of a request.
     *
     * @return Avg processing time in µs
     */
    @GetMapping("avg-time")
    public ResponseEntity<Long> getAverageResponseTime() {
        return ResponseEntity.ok(logService.getAvgResponseTime());
    }

    /**
     *
     * Returns median processing time of a request.
     *
     * @return Median processing time in µs
     */
    @GetMapping("med-time")
    public ResponseEntity<Long> getMedianResponseTime() {
        return ResponseEntity.ok(logService.getMedianResponseTime());
    }

    /**
     *
     * Returns max processing time of all requests.
     *
     * @return Highest processing time in µs
     */
    @GetMapping("max-time")
    public ResponseEntity<Long> getMaxResponseTime() {
        return ResponseEntity.ok(logService.getMaxResponseTime());
    }

    /**
     *
     * Returns all successful transactions (Status 2xx)
     *
     * @return
     */
    @GetMapping("/success")
    public ResponseEntity<List<LoggedEvent>> getSuccessfulEvents() {
        return ResponseEntity.ok(logService.findAll().stream().filter(loggedEvent -> loggedEvent.getStatusCode() >= 200 && loggedEvent.getStatusCode() <= 299).toList());
    }

    /**
     *
     * Returns all unsuccessful by client error requests (Status 4xx)
     *
     * @return
     */
    @GetMapping("/client-errors")
    public ResponseEntity<List<LoggedEvent>> getClientErrorEvents() {
        return ResponseEntity.ok(logService.findAll().stream().filter(loggedEvent -> loggedEvent.getStatusCode() >= 400 && loggedEvent.getStatusCode() <= 499).toList());
    }

    /**
     *
     * Returns all unsuccessful by server error requests (Status 5xx)
     *
     * @return
     */
    @GetMapping("/server-errors")
    public ResponseEntity<List<LoggedEvent>> getServerErrorEvents() {
        return ResponseEntity.ok(logService.findAll().stream().filter(loggedEvent -> loggedEvent.getStatusCode() >= 500 && loggedEvent.getStatusCode() <= 599).toList());
    }

    /**
     *
     * Returns ussage data for endpoint or endpoint category
     *
     * @param endpoint endpoint (e.g. "/users")
     * @return Ussage in past minute, hour and day as String
     */
    @PostMapping("/endpoint-ussage")
    public ResponseEntity<String> getEndpointUsage(@RequestBody StringRequest endpoint) {
        return ResponseEntity.ok(logService.getEndpointUssage(endpoint.getString()));
    }

    /**
     *
     * Deletes all log entries
     *
     * @return
     */
    @DeleteMapping
    public ResponseEntity<String> deleteAll() {
        logService.deleteAllLoggedEvents();
        return ResponseEntity.ok("All logs deleted");
    }

}
