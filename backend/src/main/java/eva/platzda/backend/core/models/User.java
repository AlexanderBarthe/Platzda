package eva.platzda.backend.core.models;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFlag> flags = new ArrayList<>();

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

    public void addFlag(Restaurant r){
        UserFlag uf = new UserFlag(this, r);
        flags.add(uf);
    }
    public void removeFlag(Restaurant r){
        flags.removeIf(uf -> uf.getRestaurant().getId().equals(r.getId()));
    }

    public List<Restaurant> getFlags() {
        List<Restaurant> set = new ArrayList<>();
        for(UserFlag uf : flags){
            set.add(uf.getRestaurant());
        }
        return set;
    }

    public void setFlags(List<Restaurant> flags) {
        flags.clear();
        for (Restaurant r : flags) {
            addFlag(r);
        }
    }

}
