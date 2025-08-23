package eva.platzda.backend.core.models;

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
    private boolean success;

    @Column
    private int responseTimeMs;

    @Column
    private String message;

    public LoggedEvent() {}

    public LoggedEvent(Long id, LocalDateTime timestamp, String thread, String endpoint, String eventType, boolean success, int responseTimeMs, String message) {
        this.id = id;
        this.timestamp = timestamp;
        this.thread = thread;
        this.endpoint = endpoint;
        this.eventType = eventType;
        this.success = success;
        this.responseTimeMs = responseTimeMs;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getResponseTime() {
        return responseTimeMs;
    }

    public void setResponseTime(int responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
