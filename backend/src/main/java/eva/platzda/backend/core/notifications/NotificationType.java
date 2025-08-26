package eva.platzda.backend.core.notifications;

public enum NotificationType {
    NOTIFICATION_RESTAURANT("notification_restaurant"),
    NOTIFICATION_RESERVATION("notification_reservation");

    private final String translation;

    NotificationType(String translation) {
        this.translation = translation;
    }

    public String getTranslation() {
        return translation;
    }

    public static NotificationType fromString(String translation) {
        for (NotificationType type : NotificationType.values()) {
            if (type.translation.equals(translation)) return type;
        }
        return null;
    }
}
