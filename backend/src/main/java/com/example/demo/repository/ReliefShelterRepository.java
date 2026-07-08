package com.example.demo.repository;

import com.example.demo.entity.ReliefShelter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReliefShelterRepository extends JpaRepository<ReliefShelter, Long> {
    List<ReliefShelter> findByIsActiveTrue();

    @Query("select r from ReliefShelter r where r.isActive = true and (r.capacity - r.currentOccupancy) >= :needed")
    List<ReliefShelter> findWithAvailableCapacity(@Param("needed") int needed);
}
