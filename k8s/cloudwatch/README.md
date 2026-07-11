# AWS CloudWatch — Monitoreo y logging (IE11 / IE12)

Complementa a Prometheus/Grafana enviando los **logs** de la aplicación a
**Amazon CloudWatch Logs**, donde el equipo puede consultarlos, filtrarlos y
crear alarmas.

## Aplicar

```bash
kubectl apply -f k8s/cloudwatch/fluent-bit.yaml
# Ver los logs en la consola AWS: CloudWatch > Log groups >
#   /aws/eks/ep3-cluster/application
```

## Métricas de recursos en CloudWatch (opcional)

Para ver CPU/memoria/red por pod y nodo en CloudWatch (Container Insights):

```bash
ClusterName=ep3-cluster
RegionName=us-east-1
curl -s https://raw.githubusercontent.com/aws-samples/amazon-cloudwatch-container-insights/latest/k8s-deployment-manifest-templates/deployment-mode/daemonset/container-insights-monitoring/quickstart/cwagent-fluent-bit-quickstart.yaml \
  | sed "s/{{cluster_name}}/$ClusterName/;s/{{region_name}}/$RegionName/" \
  | kubectl apply -f -
```

## Rol en la evaluación

- **Logging (IE11):** trazabilidad centralizada de todos los contenedores.
- **Métricas (IE12):** análisis de métricas de uso en CloudWatch, además de
  los dashboards de Grafana.
- Los nodos usan `LabRole`, que ya incluye permisos de CloudWatch.
