package eva.platzda.backend.core.models;


import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurant")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String address;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User owner;

    @Column
    private Integer timeSlotDuration;


    public Restaurant() {
    }

    public Restaurant(Long id, String address, User owner, int timeSlotDuration) {
        this.id = id;
        this.address = address;
        this.owner = owner;
        this.timeSlotDuration = timeSlotDuration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Integer getTimeSlotDuration() {
        return timeSlotDuration;
    }

    public void setTimeSlotDuration(Integer timeSlotDuration) {
        this.timeSlotDuration = timeSlotDuration;
    }

    @ElementCollection
    @CollectionTable(
            name = "restaurant_tags",
            joinColumns = @JoinColumn(name = "restaurant_id")
    )
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }


}
