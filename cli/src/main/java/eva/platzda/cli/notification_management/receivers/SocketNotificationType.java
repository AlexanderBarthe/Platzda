package eva.platzda.cli.notification_management.receivers;

public enum SocketNotificationType {
    ANSWER("answer"),
    NOTIFICATION_RESTAURANT("notification_restaurant"),
    NOTIFICATION_RESERVATION("notification_reservation");

    private final String translation;

    SocketNotificationType(String translation) {
        this.translation = translation;
    }

    public String getTranslation() {
        return translation;
    }

    public static SocketNotificationType fromString(String translation) {
        for (SocketNotificationType type : SocketNotificationType.values()) {
            if (type.translation.equals(translation)) return type;
        }
        return null;
    }
}
