package cl.duoc.devops.product;

/**
 * Se lanza cuando se solicita un producto que no existe.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Producto no encontrado con id: " + id);
    }
}
