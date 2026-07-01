package cl.duoc.devops.product;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lógica de negocio del inventario.
 * Usa un almacenamiento en memoria (suficiente para el entorno simulado del encargo)
 * y registra métricas de negocio personalizadas en Micrometer (observabilidad - IE1/IE3).
 */
@Service
public class ProductService {

    private final ConcurrentHashMap<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    // Métricas de negocio personalizadas que Prometheus podrá scrapear.
    private final Counter productsCreatedCounter;
    private final Counter productNotFoundCounter;

    public ProductService(MeterRegistry registry) {
        // "created" es palabra reservada en Prometheus (se usa para _created timestamps),
        // por eso usamos "products_registered". Micrometer agrega el sufijo "_total".
        this.productsCreatedCounter = Counter.builder("products_registered")
                .description("Cantidad total de productos creados")
                .register(registry);
        this.productNotFoundCounter = Counter.builder("products_not_found")
                .description("Cantidad de búsquedas de productos inexistentes")
                .register(registry);

        // Gauge: cantidad de productos en el inventario en tiempo real.
        registry.gauge("products_in_inventory", store, ConcurrentHashMap::size);
    }

    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    public Product findById(Long id) {
        Product product = store.get(id);
        if (product == null) {
            productNotFoundCounter.increment();
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    public Product create(Product product) {
        long id = sequence.incrementAndGet();
        product.setId(id);
        store.put(id, product);
        productsCreatedCounter.increment();
        return product;
    }

    public Product update(Long id, Product changes) {
        Product existing = findById(id);
        existing.setName(changes.getName());
        existing.setPrice(changes.getPrice());
        existing.setStock(changes.getStock());
        return existing;
    }

    public void delete(Long id) {
        Optional<Product> existing = Optional.ofNullable(store.remove(id));
        if (existing.isEmpty()) {
            productNotFoundCounter.increment();
            throw new ProductNotFoundException(id);
        }
    }
}
