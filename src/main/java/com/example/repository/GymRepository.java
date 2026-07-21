package com.example.repository;

import com.example.model.Gym;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GymRepository extends CrudRepository<Gym, Long> {
    Optional<Gym> findByName(String name);
}
