package eva.platzda.backend.core.models;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appuser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String email;

    public User() {

    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public User(String name, String email){
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ManyToMany
    @JoinTable(
            name = "user_flag",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> flags = new ArrayList<>();

    public List<Restaurant> getFlags() {
        return flags;
    }

    public void setFlags(List<Restaurant> flags) {
        this.flags = flags;
    }

    public void addFlag(Restaurant restaurant) {
        this.flags.add(restaurant);
    }

    public void removeFlag(Restaurant restaurant) {
        this.flags.remove(restaurant);
    }
}
