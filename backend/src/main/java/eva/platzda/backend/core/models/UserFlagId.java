package eva.platzda.backend.core.models;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class UserFlagId implements Serializable {
    private Long userId;
    private Long restaurantId;

    public UserFlagId(Long userId, Long restaurantId) {
        this.userId = userId;
        this.restaurantId = restaurantId;
    }

    public UserFlagId() {

    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public boolean equals(Object other) {
        if (!(other instanceof UserFlagId id)) {
            return false;
        }
        if(id.userId.equals(this.userId) && id.restaurantId.equals(this.restaurantId)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return userId.hashCode() + restaurantId.hashCode();
    }

}
