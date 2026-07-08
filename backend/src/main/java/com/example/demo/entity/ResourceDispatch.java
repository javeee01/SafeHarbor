package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_dispatches")
public class ResourceDispatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_incident_id", nullable = false)
    private DisasterIncident targetIncident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private SupplyInventory inventory;

    @Column(name = "dispatched_quantity", nullable = false)
    private Integer dispatchedQuantity;

    @Column(name = "dispatch_status", nullable = false)
    private String dispatchStatus;

    private LocalDateTime initiatedAt;

    @PrePersist
    void prePersist() {
        if (dispatchStatus == null) dispatchStatus = "IN_TRANSIT";
        if (initiatedAt == null) initiatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DisasterIncident getTargetIncident() { return targetIncident; }
    public void setTargetIncident(DisasterIncident targetIncident) { this.targetIncident = targetIncident; }
    public SupplyInventory getInventory() { return inventory; }
    public void setInventory(SupplyInventory inventory) { this.inventory = inventory; }
    public Integer getDispatchedQuantity() { return dispatchedQuantity; }
    public void setDispatchedQuantity(Integer dispatchedQuantity) { this.dispatchedQuantity = dispatchedQuantity; }
    public String getDispatchStatus() { return dispatchStatus; }
    public void setDispatchStatus(String dispatchStatus) { this.dispatchStatus = dispatchStatus; }
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
    public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
}
