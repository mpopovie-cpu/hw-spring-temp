package com.smartshop.inventory;

import com.smartshop.category.Category;
import com.smartshop.exception.BusinessException;
import com.smartshop.exception.InsufficientStockException;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.product.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void createsInventory() {
        Product product = mockProduct(1L);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory inventory = inventoryService.createInventory(product, 50);

        assertThat(inventory.getQuantity()).isEqualTo(50);
        assertThat(inventory.getReservedQuantity()).isZero();
    }

    @Test
    void addsStock() {
        Inventory inventory = new Inventory(10, 0, LocalDateTime.now(), mockProduct(1L));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.addStock(1L, 5);

        assertThat(inventory.getQuantity()).isEqualTo(15);
    }

    @Test
    void removesStock() {
        Inventory inventory = new Inventory(10, 0, LocalDateTime.now(), mockProduct(1L));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.removeStock(1L, 4);

        assertThat(inventory.getQuantity()).isEqualTo(6);
    }

    @Test
    void rejectsRemovingMoreThanPhysicalStock() {
        Inventory inventory = new Inventory(5, 0, LocalDateTime.now(), mockProduct(1L));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.removeStock(1L, 10))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void rejectsRemovingMoreThanAvailableWhenStockIsReserved() {
        // quantity=10, reserved=6 -> available=4, попытка снять 5 должна упасть
        Inventory inventory = new Inventory(10, 6, LocalDateTime.now(), mockProduct(1L));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.removeStock(1L, 5))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void reservesStockWhenAvailable() {
        Inventory inventory = new Inventory(10, 2, LocalDateTime.now(), mockProduct(1L));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.reserveStock(1L, 5);

        assertThat(inventory.getReservedQuantity()).isEqualTo(7);
    }

    @Test
    void rejectsReservingMoreThanAvailable() {
        Inventory inventory = new Inventory(10, 8, LocalDateTime.now(), mockProduct(1L));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.reserveStock(1L, 5))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void releasesReservedStock() {
        Inventory inventory = new Inventory(10, 5, LocalDateTime.now(), mockProduct(1L));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.releaseStock(1L, 3);

        assertThat(inventory.getReservedQuantity()).isEqualTo(2);
    }

    @Test
    void rejectsReleasingMoreThanReserved() {
        Inventory inventory = new Inventory(10, 2, LocalDateTime.now(), mockProduct(1L));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.releaseStock(1L, 5))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void calculatesAvailableStock() {
        Inventory inventory = new Inventory(10, 4, LocalDateTime.now(), mockProduct(1L));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        int available = inventoryService.getAvailableStock(1L);

        assertThat(available).isEqualTo(6);
    }

    @Test
    void rejectsNonPositiveAmounts() {
        assertThatThrownBy(() -> inventoryService.addStock(1L, 0))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void throwsWhenInventoryMissing() {
        when(inventoryRepository.findByProductId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryByProductId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Product mockProduct(Long id) {
        Category category = new Category("Category" + id, "desc", true);
        return new Product("Product" + id, "desc", new BigDecimal("10.00"), true,
                LocalDateTime.now(), LocalDateTime.now(), category);
    }
}