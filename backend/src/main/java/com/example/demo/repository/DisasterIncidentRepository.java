package com.example.demo.repository;

import com.example.demo.entity.DisasterIncident;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DisasterIncidentRepository extends JpaRepository<DisasterIncident, Long> {
    Page<DisasterIncident> findByStatusNot(String status, Pageable pageable);

    @Query("select d from DisasterIncident d where d.assignedResponder is null and upper(d.severityLevel) in ('HIGH', 'CRITICAL')")
    List<DisasterIncident> findCriticalUnassigned();
}
