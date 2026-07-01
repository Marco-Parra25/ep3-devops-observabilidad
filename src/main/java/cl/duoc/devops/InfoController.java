package cl.duoc.devops;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint informativo del servicio.
 * Útil para demostrar el despliegue automático: al cambiar APP_VERSION y
 * hacer push, el pipeline redepliega y la URL en la nube refleja el cambio.
 */
@RestController
@RequestMapping("/api")
public class InfoController {

    // 👇 Cambia este valor para demostrar un despliegue en el video.
    private static final String APP_VERSION = "1.0.0";

    @GetMapping("/version")
    public Map<String, String> version() {
        return Map.of(
                "servicio", "observability-service",
                "version", APP_VERSION,
                "mensaje", "Despliegue automatico en AWS EKS"
        );
    }
}
