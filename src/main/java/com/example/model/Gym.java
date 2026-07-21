package com.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "gyms")
@Getter
@Setter
public class Gym {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String address;
    private String city;
    private String country;
    private String phone;
    private String website;
    private String description;
    private String openingHours;
    private String subscriptionStatus = "FREE";

    @OneToMany(mappedBy = "gym")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<User> trainers;

    public Gym() {}

    public Gym(String name) {
        this.name = name;
    }
}
