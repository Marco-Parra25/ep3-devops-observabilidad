# Informe — Evaluación Parcial 3
## Observabilidad y entornos reales en DevOps (DOY0101)

**Integrantes:** _(nombres)_
**Fecha:** _(fecha de entrega)_
**Repositorio:** https://github.com/Marco-Parra25/ep3-devops-observabilidad

---

## 1. Introducción

_(Breve descripción del proyecto y su objetivo: extender un pipeline DevOps con observabilidad, métricas y cumplimiento. 1-2 párrafos escritos por el equipo.)_

## 2. Arquitectura de la solución

_(Insertar el diagrama de arquitectura — pueden usar el del README — y describir el flujo con sus propias palabras.)_

> 📎 **Captura sugerida:** diagrama de arquitectura.

---

## 3. Desarrollo por indicador

### IE1 — Monitoreo y logging

**Qué se implementó:** Spring Boot Actuator expone `/actuator/health`, `/actuator/metrics` y `/actuator/prometheus`. Micrometer publica métricas de la JVM (CPU, memoria), de HTTP (peticiones, errores 4xx/5xx, latencia) y métricas de negocio propias (`products_registered_total`, `products_in_inventory`, `products_not_found_total`). Prometheus recolecta estas métricas y hay reglas de alerta para detectar anomalías (servicio caído, tasa de errores alta, memoria elevada).

> 📎 **Capturas:** Prometheus con targets UP; endpoint `/actuator/prometheus`; archivo `monitoring/prometheus/alert.rules.yml`.

**Justificación técnica:** _(¿Por qué eligieron estas métricas? ¿Cómo ayudan a detectar anomalías? — escribir sin IA.)_

### IE2 — Despliegue en Kubernetes en la nube

**Qué se implementó:** cluster **AWS EKS** (`ep3-cluster`, 2 nodos) creado con eksctl. El microservicio corre con **2 réplicas** distribuidas en los nodos, expuesto con un **LoadBalancer** público. La imagen se publica en **Amazon ECR**. El despliegue incluye probes de liveness/readiness, límites de CPU/memoria, `securityContext` (no-root, filesystem de solo lectura) y anotaciones para que Prometheus descubra los pods automáticamente.

> 📎 **Capturas:** `kubectl get nodes`, `kubectl get pods -n devops-ep3`, `kubectl top pods`, la API respondiendo desde la URL del LoadBalancer de AWS.

**Justificación técnica:** _(¿Por qué 2 réplicas? ¿Qué aporta el LoadBalancer y las probes? — escribir sin IA.)_

### IE3 — Dashboards con métricas clave

**Qué se implementó:** dashboard en **Grafana** con las cuatro métricas exigidas: **tiempo de despliegue** y **cobertura de pruebas** (empujadas desde el pipeline vía Pushgateway), **uso de CPU/memoria** y **errores registrados**, además de disponibilidad, latencia y métricas de negocio.

> 📎 **Capturas:** dashboard de Grafana completo con datos.

**Justificación técnica:** _(¿Qué decisiones permiten tomar estas métricas? — escribir sin IA.)_

### IE4 — Documentación de la integración en CI/CD

**Qué se implementó:** el pipeline (`.github/workflows/ci-cd.yml`) integra build, pruebas, cobertura (JaCoCo), calidad (SonarCloud) y seguridad (Trivy). _(Ver sección 4.)_

**Justificación técnica:** _(¿Cómo esta integración permite tomar decisiones técnicas informadas y mejorar la calidad continua? — escribir sin IA. Esta es una sección central de la nota.)_

### IE5 — Políticas de cumplimiento y auditoría

**Qué se implementó:**
- **SonarCloud** analiza calidad y seguridad del código en cada PR (Quality Gate).
- **Branch protection** en `main`: exige Pull Request y que pasen los checks obligatorios antes de fusionar.
- **Trivy** audita vulnerabilidades de dependencias en cada ejecución.

> 📎 **Capturas:** dashboard de SonarCloud; configuración de branch protection (Settings → Branches); un PR mostrando los checks requeridos.

**Justificación técnica:** _(¿Por qué estas políticas garantizan calidad, seguridad y trazabilidad? — escribir sin IA.)_

### IE6 — El pipeline se detiene ante fallas críticas

**Qué se implementó y demostró:** se creó un Pull Request (#2) que introdujo a propósito una dependencia vulnerable (**Log4Shell, CVE-2021-44228**). El pipeline detectó la vulnerabilidad con Trivy, **falló** y **bloqueó la fusión**. Un PR sin problemas (#1) sí pudo fusionarse.

> 📎 **Capturas:** PR #2 con el check en rojo y "merge bloqueado"; el log de Trivy mostrando CVE-2021-44228 CRITICAL; contraste con PR #1 (verde).

**Justificación técnica:** _(¿Por qué es importante que el pipeline se detenga? ¿Qué protege? — escribir sin IA.)_

---

## 4. Integración de herramientas en el pipeline CI/CD

_(Explicar con sus palabras cómo se integran monitoreo, métricas y seguridad en el ciclo de vida, y cómo cada herramienta aporta a la mejora continua y a la toma de decisiones. Apóyense en la sección "El pipeline CI/CD" del README, pero la explicación y justificación deben ser propias.)_

---

## 5. Conclusiones

_(Conclusiones del equipo sobre el trabajo realizado y los aprendizajes. **Redactar sin apoyo de IA** — requisito de la evaluación.)_

---

## 6. Reflexiones individuales

> ⚠️ **Obligatorio y sin IA.** Cada integrante escribe su propia reflexión sobre su aprendizaje y aporte al proyecto.

**Integrante 1 — _(nombre)_:**
_(reflexión personal)_

**Integrante 2 — _(nombre)_:**
_(reflexión personal)_

---

## 7. Declaración de uso de IA

Se utilizó **Claude Code (Anthropic)** como apoyo para generación/depuración de código, configuración de herramientas, redacción de documentación técnica y diagramas. Las decisiones técnicas, justificaciones, conclusiones y reflexiones individuales fueron elaboradas por el equipo sin IA. Todo el contenido asistido por IA fue revisado y validado.

Referencia: https://bibliotecas.duoc.cl/ia

## 8. Referencias

_(Documentación de Spring Boot, Prometheus, Grafana, AWS EKS, SonarCloud, Trivy, etc.)_
