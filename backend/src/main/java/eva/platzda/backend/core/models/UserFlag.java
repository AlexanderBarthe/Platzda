package eva.platzda.backend.core.models;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "user_flag")
public class UserFlag {

    @EmbeddedId
    private UserFlagId id = new UserFlagId();

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // erzeugt DB FK mit ON DELETE CASCADE
    private User user;

    @MapsId("restaurantId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // ebenfalls ON DELETE CASCADE
    private Restaurant restaurant;

    public UserFlag() {

    }

    public UserFlag(User user, Restaurant restaurant) {
        this.user = user;
        this.restaurant = restaurant;
    }

    public UserFlagId getId() {
        return id;
    }
    public void setId(UserFlagId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

}
