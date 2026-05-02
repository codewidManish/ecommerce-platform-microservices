package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    Optional<Inventory> findByProductSku(String sku);

    @Query("SELECT i FROM Inventory i WHERE (i.quantity - i.reservedQuantity) <= i.lowStockThreshold")
    List<Inventory> findLowStockItems();

    boolean existsByProductId(Long productId);
}
