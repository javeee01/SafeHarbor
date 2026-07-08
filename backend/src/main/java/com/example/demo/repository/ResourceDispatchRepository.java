package com.example.demo.repository;

import com.example.demo.entity.ResourceDispatch;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceDispatchRepository extends JpaRepository<ResourceDispatch, Long> {
    List<ResourceDispatch> findByDispatchStatus(String dispatchStatus);
    List<ResourceDispatch> findByTargetIncidentId(Long incidentId);
}
