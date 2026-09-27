package com.smartshop.inventory;

import com.smartshop.exception.InsufficientStockException;
import com.smartshop.exception.BusinessException;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.product.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id=" + productId));
    }

    @Transactional
    public Inventory createInventory(Product product, Integer quantity) {
        Inventory inventory = new Inventory(quantity, 0, LocalDateTime.now(), product);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public void addStock(Long productId, Integer amount) {
        validatePositiveAmount(amount);
        Inventory inventory = getInventoryByProductId(productId);
        inventory.setQuantity(inventory.getQuantity() + amount);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void removeStock(Long productId, Integer amount) {
        validatePositiveAmount(amount);
        Inventory inventory = getInventoryByProductId(productId);
        // нельзя уходить в минус по доступному остатку
        int available = inventory.getQuantity() - inventory.getReservedQuantity();
        if (amount > available) {
            throw new InsufficientStockException("Cannot remove more than available stock: productId=" + productId);
        }
        inventory.setQuantity(inventory.getQuantity() - amount);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void reserveStock(Long productId, Integer amount) {
        validatePositiveAmount(amount);
        Inventory inventory = getInventoryByProductId(productId);
        int available = inventory.getQuantity() - inventory.getReservedQuantity();
        if (amount > available) {
            throw new InsufficientStockException("Insufficient available stock for productId=" + productId);
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() + amount);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void releaseStock(Long productId, Integer amount) {
        validatePositiveAmount(amount);
        Inventory inventory = getInventoryByProductId(productId);
        // резерв не может уйти в минус
        if (amount > inventory.getReservedQuantity()) {
            throw new InsufficientStockException("Cannot release more than reserved for productId=" + productId);
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() - amount);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public int getAvailableStock(Long productId) {
        Inventory inventory = getInventoryByProductId(productId);
        return inventory.getQuantity() - inventory.getReservedQuantity();
    }

    private void validatePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException("Amount must be greater than 0");
        }
    }
}