package eva.platzda.backend.logging;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "logged_events")
public class LoggedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime timestamp;

    @Column
    private String thread;

    @Column
    private String endpoint;

    @Column
    private String eventType; //Get Request / Deletion / Notification etc

    @Column
    private Integer statusCode;

    @Column
    private Long responseTimeUs;

    @Column
    private String message;

    public LoggedEvent() {}

    public LoggedEvent(String endpoint, String eventType, Integer statusCode, long responseTimeUs, String message) {
        this.id = null;
        this.timestamp = LocalDateTime.now();
        this.thread = Thread.currentThread().getName();
        this.endpoint = endpoint;
        this.eventType = eventType;
        this.statusCode = statusCode;
        this.responseTimeUs = responseTimeUs;
        this.message = message;
    }

    public LoggedEvent(Long id, LocalDateTime timestamp, String thread, String endpoint, String eventType, Integer statusCode, long responseTimeUs, String message) {
        this.id = id;
        this.timestamp = timestamp;
        this.thread = thread;
        this.endpoint = endpoint;
        this.eventType = eventType;
        this.statusCode = statusCode;
        this.responseTimeUs = responseTimeUs;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer success) {
        this.statusCode = success;
    }

    public long getResponseTime_us() {
        return responseTimeUs;
    }

    public void setResponseTime(Long responseTimeMs) {
        this.responseTimeUs = responseTimeMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
