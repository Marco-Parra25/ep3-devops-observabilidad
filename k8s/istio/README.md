# Istio — malla de servicios (IE12)

Istio se usa para **manejar las redes y las métricas de los servicios**: enrutamiento,
balanceo de carga, resiliencia (reintentos y circuit breaker), seguridad (mTLS) y
métricas de tráfico automáticas (latencia, errores, RPS) hacia Prometheus/Grafana.

## Instalación en el cluster EKS

```bash
# 1. Instalar Istio (perfil demo) con istioctl
istioctl install --set profile=demo -y

# 2. Habilitar la inyección automática del sidecar Envoy en el namespace
kubectl label namespace devops-ep3 istio-injection=enabled --overwrite

# 3. Reiniciar el deployment para que se inyecte el sidecar
kubectl rollout restart deployment/observability-service -n devops-ep3

# 4. Aplicar las reglas de red y seguridad de Istio
kubectl apply -f k8s/istio/observability-istio.yaml

# 5. (Opcional) Dashboards de la malla
istioctl dashboard kiali      # topología y tráfico
istioctl dashboard grafana    # métricas de Istio
```

## Qué aporta a la evaluación

| Recurso | Rol |
|---------|-----|
| `Gateway` + `VirtualService` | Entrada y enrutamiento del tráfico al servicio |
| `DestinationRule` | Balanceo ROUND_ROBIN + circuit breaker (outlier detection) |
| `PeerAuthentication` (mTLS STRICT) | Cifrado del tráfico entre servicios (seguridad) |
| Sidecar Envoy | Métricas de red automáticas (latencia, tasa de error, RPS) |

> El `Service` de tipo LoadBalancer (`k8s/03-service.yaml`) sigue disponible; con
> Istio el tráfico también puede entrar por el `istio-ingressgateway`.
