package com.example.demo.repository;

import com.example.demo.entity.SupplyInventory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SupplyInventoryRepository extends JpaRepository<SupplyInventory, Long> {
    Optional<SupplyInventory> findByItemName(String itemName);

    @Query("select s from SupplyInventory s where s.availableQuantity <= s.criticalThreshold")
    List<SupplyInventory> findItemsBelowCriticalThreshold();
}
