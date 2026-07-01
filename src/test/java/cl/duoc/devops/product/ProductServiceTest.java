package cl.duoc.devops.product;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la lógica de negocio del inventario.
 */
class ProductServiceTest {

    private ProductService service;

    @BeforeEach
    void setUp() {
        // SimpleMeterRegistry: registro de métricas en memoria para pruebas.
        service = new ProductService(new SimpleMeterRegistry());
    }

    @Test
    void createAssignsIdAndStoresProduct() {
        Product created = service.create(new Product(null, "Teclado", 15000.0, 10));

        assertNotNull(created.getId());
        assertEquals("Teclado", created.getName());
        assertEquals(1, service.findAll().size());
    }

    @Test
    void findByIdReturnsExistingProduct() {
        Product created = service.create(new Product(null, "Mouse", 8000.0, 20));

        Product found = service.findById(created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        assertThrows(ProductNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    void updateModifiesExistingProduct() {
        Product created = service.create(new Product(null, "Monitor", 90000.0, 5));

        Product updated = service.update(created.getId(),
                new Product(null, "Monitor 4K", 120000.0, 3));

        assertEquals("Monitor 4K", updated.getName());
        assertEquals(120000.0, updated.getPrice());
        assertEquals(3, updated.getStock());
    }

    @Test
    void updateThrowsWhenMissing() {
        assertThrows(ProductNotFoundException.class,
                () -> service.update(999L, new Product(null, "X", 1.0, 1)));
    }

    @Test
    void deleteRemovesProduct() {
        Product created = service.create(new Product(null, "Cable", 3000.0, 100));

        service.delete(created.getId());

        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void deleteThrowsWhenMissing() {
        assertThrows(ProductNotFoundException.class, () -> service.delete(999L));
    }
}
