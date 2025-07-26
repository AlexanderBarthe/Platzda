package com.eva.platzda.models;


import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    private int timeSlotDuration;


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

    public int getTimeSlotDuration() {
        return timeSlotDuration;
    }

    public void setTimeSlotDuration(int timeSlotDuration) {
        this.timeSlotDuration = timeSlotDuration;
    }
}
