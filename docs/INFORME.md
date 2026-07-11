# Informe — Ingeniería DevOps (DOY0101)
## Observabilidad y entornos reales en DevOps

**Integrantes:** Marco Parra / Luis Inostroza
**Fecha:** 2 de julio de 2026
**Repositorio:** https://github.com/Marco-Parra25/ep3-devops-observabilidad

---

## 1. Introducción

El presente informe documenta la extensión de un pipeline DevOps para incorporar mecanismos de observabilidad, métricas, dashboards y políticas de cumplimiento automatizado, desplegando un microservicio en un entorno de Kubernetes en la nube (AWS EKS). El objetivo es garantizar una operación confiable, transparente y alineada a estándares de calidad, cubriendo la trazabilidad completa desde el desarrollo hasta la producción simulada.

El proyecto integra herramientas de monitoreo (Prometheus, Grafana), análisis de calidad y seguridad (SonarCloud, Trivy) y despliegue continuo automatizado, de modo que cada cambio en el código se valida y se publica en la nube de forma automática.

## 2. Arquitectura de la solución

La solución sigue un flujo de izquierda a derecha: un cambio en el código entra por un Pull Request en GitHub; el pipeline de integración continua lo compila, prueba y analiza; si supera todos los controles, la imagen del contenedor se publica en Amazon ECR y se despliega automáticamente en el cluster de Kubernetes (AWS EKS). Dentro del cluster, el microservicio corre con dos réplicas y es monitoreado por Prometheus, cuyas métricas se visualizan en Grafana. Adicionalmente, la malla de servicios **Istio** gestiona la red y las métricas de tráfico entre servicios, y **Fluent Bit** envía los logs a **AWS CloudWatch**.

> 📎 **Captura sugerida:** diagrama de arquitectura.

---

## 3. Desarrollo por indicador

### IE1 — Monitoreo y logging

**Qué se implementó:** Spring Boot Actuator expone `/actuator/health`, `/actuator/metrics` y `/actuator/prometheus`. Micrometer publica métricas de la JVM (CPU, memoria), de HTTP (peticiones, errores 4xx/5xx, latencia) y métricas de negocio propias (`products_registered_total`, `products_in_inventory`, `products_not_found_total`). Prometheus recolecta estas métricas y hay reglas de alerta para detectar anomalías (servicio caído, tasa de errores alta, memoria elevada). Los logs, además, se centralizan en AWS CloudWatch mediante Fluent Bit.

> 📎 **Capturas:** Prometheus con targets UP; endpoint `/actuator/prometheus`; archivo `monitoring/prometheus/alert.rules.yml`; log group en CloudWatch.

**Justificación técnica:** Al abrir Grafana pudimos visualizar los gráficos con las métricas del microservicio. La aplicación genera las métricas en formato de texto en el endpoint `/actuator/prometheus`, Prometheus las lee y almacena periódicamente, y Grafana las consulta para graficarlas. Elegimos este esquema porque desacopla la aplicación de la herramienta de visualización: la app solo se encarga de exponer los datos, sin depender de Prometheus ni Grafana, lo que permite cambiarlas en el futuro sin modificar el código.

### IE2 — Despliegue en Kubernetes en la nube

**Qué se implementó:** cluster **AWS EKS** (2 nodos) creado con eksctl. El microservicio corre con **2 réplicas** distribuidas en los nodos, expuesto con un **LoadBalancer** público. La imagen se publica en **Amazon ECR**. El despliegue incluye probes de liveness/readiness, límites de CPU/memoria, `securityContext` (no-root, filesystem de solo lectura) y anotaciones para que Prometheus descubra los pods automáticamente.

> 📎 **Capturas:** `kubectl get nodes`, `kubectl get pods -n devops-ep3`, `kubectl top pods`, la API respondiendo desde la URL del LoadBalancer de AWS.

**Justificación técnica:** Configuramos el despliegue con 2 réplicas (2 pods) en lugar de una, para garantizar la alta disponibilidad del servicio. Con una sola instancia, si ese pod o su nodo falla, toda la aplicación quedaría caída (un único punto de falla). Con dos réplicas, si una se cae, la otra sigue atendiendo las peticiones, de modo que el sistema permanece disponible. Además, el LoadBalancer reparte el tráfico entre ambas, y Kubernetes reinicia automáticamente cualquier pod que deje de responder.

### IE3 — Dashboards con métricas clave

**Qué se implementó:** dashboard en **Grafana** con las cuatro métricas exigidas: **tiempo de despliegue** y **cobertura de pruebas** (empujadas desde el pipeline vía Pushgateway), **uso de CPU/memoria** y **errores registrados**, además de disponibilidad, latencia y métricas de negocio.

> 📎 **Capturas:** dashboard de Grafana completo con datos.

**Justificación técnica:** El dashboard nos permite ver en un solo lugar cómo el funcionamiento de la aplicación afecta a los recursos del nodo (la instancia EC2 donde corre): cuánta CPU y memoria consume. Con esa información podemos detectar si el servicio necesita más recursos y dimensionarlos según la demanda, además de vigilar los errores y la cobertura de pruebas. Tener todas las métricas juntas facilita tomar decisiones técnicas rápidas sobre el estado y la capacidad del sistema.

### IE4 — Documentación de la integración en CI/CD

**Qué se implementó:** el pipeline (`.github/workflows/ci-cd.yml`) integra build, pruebas, cobertura (JaCoCo), calidad (SonarCloud), seguridad (Trivy), pruebas de aceptación, publicación de imagen y despliegue automático a EKS. _(Ver sección 4.)_

**Justificación técnica:** Documentar cómo se integran las herramientas en el pipeline permite que cualquier integrante o evaluador entienda de qué manera cada una (pruebas, cobertura, SonarCloud, Trivy, despliegue) aporta al proceso y a la toma de decisiones técnicas. Esta trazabilidad facilita mantener y mejorar el sistema de forma continua.

### IE5 — Políticas de cumplimiento y auditoría

**Qué se implementó:**
- **SonarCloud** analiza calidad y seguridad del código en cada PR (Quality Gate).
- **Branch protection** en `main`: exige Pull Request y que pasen los checks obligatorios antes de fusionar.
- **Trivy** audita vulnerabilidades de dependencias en cada ejecución.
- **Dependabot** abre PRs automáticos ante dependencias vulnerables o desactualizadas.

> 📎 **Capturas:** dashboard de SonarCloud; configuración de branch protection (Settings → Branches); un PR mostrando los checks requeridos.

**Justificación técnica:** Aplicamos protección de rama sobre main para que ningún cambio pueda fusionarse sin antes pasar por un Pull Request y aprobar los controles automáticos (pruebas, análisis de calidad con SonarCloud y escaneo de seguridad con Trivy). Esto evita que código con errores o vulnerabilidades llegue a main, que es la rama que se despliega a producción. De este modo garantizamos trazabilidad (todo cambio queda registrado en un PR) y reducimos el riesgo de romper el sistema en producción.

### IE6 — El pipeline se detiene ante fallas críticas

**Qué se implementó y demostró:** se creó un Pull Request (#2) que introdujo a propósito una dependencia vulnerable (**Log4Shell, CVE-2021-44228**). El pipeline detectó la vulnerabilidad con Trivy, **falló** y **bloqueó la fusión**. Un PR sin problemas (#1) sí pudo fusionarse.

> 📎 **Capturas:** PR #2 con el check en rojo y "merge bloqueado"; el log de Trivy mostrando CVE-2021-44228 CRITICAL; contraste con PR #1 (verde).

**Justificación técnica:** El pipeline se detiene automáticamente ante una falla crítica para proteger la integridad del código que llega a producción. Estas fallas pueden ser de seguridad (una vulnerabilidad detectada por Trivy), de calidad (el análisis de SonarCloud) o de pruebas (tests que fallan o cobertura por debajo del umbral). Lo demostramos introduciendo a propósito una dependencia vulnerable (Log4Shell, CVE-2021-44228): Trivy la detectó, el pipeline falló y la fusión quedó bloqueada. Preferimos un control que bloquea en vez de uno que solo avisa, porque obliga a corregir el problema antes de continuar, evitando que llegue al entorno productivo.

---

## 4. Integración de herramientas en el pipeline CI/CD

El pipeline está definido en `.github/workflows/ci-cd.yml` y se ejecuta en cada push y Pull Request. Se compone de las siguientes etapas:

1. **Build, test y cobertura:** compila el microservicio, ejecuta las pruebas y valida con JaCoCo que la cobertura sea de al menos 70 %. Si baja del umbral, el pipeline falla.
2. **Análisis de seguridad (Trivy):** escanea las dependencias en busca de vulnerabilidades; ante hallazgos críticos o altos, detiene el pipeline.
3. **Análisis de calidad (SonarCloud):** analiza el código en cada Pull Request y publica un Quality Gate.
4. **Pruebas de aceptación:** levanta la imagen real del contenedor y valida los endpoints de salud y métricas antes de permitir el despliegue.
5. **Publicación de imagen:** publica la imagen en el registro de contenedores (GHCR) cuando todo lo anterior pasa.
6. **Despliegue automático (CD):** al fusionar a `main`, construye la imagen, la publica en Amazon ECR y actualiza el despliegue en EKS (`kubectl set image` + rollout), sin intervención manual.

Las etapas de control son obligatorias por la protección de rama: ningún cambio se fusiona a `main` sin que todas pasen.

---

## 5. Conclusiones

En este proyecto logramos integrar un flujo de CI/CD completo: partiendo de una aplicación ejecutada localmente, esta se empaqueta como una imagen de contenedor, se publica en Amazon ECR y se despliega automáticamente en el cluster de AWS EKS, que la ejecuta en pods distribuidos sobre los nodos (instancias EC2). Además, mediante GitHub Actions incorporamos controles de calidad y seguridad (SonarCloud y Trivy) y protección de rama, de modo que ningún cambio llega a main —ni al entorno desplegado— sin antes aprobar las pruebas necesarias. Esto nos permitió comprender de extremo a extremo cómo se automatiza el ciclo de vida del software, desde el desarrollo hasta el despliegue en la nube, con observabilidad y cumplimiento.

---

## 6. Reflexiones individuales

> ⚠️ Redactadas por cada integrante, sin apoyo de IA.

**Marco Parra:**
Al iniciar el proyecto no sabía qué eran un cluster, los nodos, los pods ni ECR, ni cómo interactuaban entre sí. Lo más valioso para mí fue lograr entender esa simbiosis: cómo toda la logística se conecta para que la aplicación funcione de extremo a extremo, desde el código local hasta el despliegue en la nube. También aprendí el flujo completo de CI/CD y a trabajar de forma colaborativa con mi compañero. Mi aporte estuvo en la construcción, prueba y validación del pipeline y del despliegue del microservicio.

**Luis Inostroza:**
Lo más desafiante para mí fue comprender el flujo completo de CI/CD y cómo se conectan las distintas etapas del pipeline hasta llegar al despliegue automático en la nube. Trabajar en equipo me ayudó a resolver estas dificultades y a entender cómo interactúan las herramientas de integración y despliegue continuo. Aprendí el valor de automatizar el proceso y de contar con controles de calidad y seguridad antes de publicar cambios.

---

## 7. Declaración de uso de IA

Durante el desarrollo se utilizó el asistente **Claude Code (Anthropic)** como apoyo para la generación y depuración de código, la configuración de herramientas (pipeline, manifiestos de Kubernetes, monitoreo), la redacción de la documentación técnica y la creación de diagramas.

Todas las decisiones técnicas, justificaciones, conclusiones y reflexiones individuales fueron elaboradas por el equipo sin asistencia de IA, conforme a las indicaciones de la evaluación. El contenido generado con apoyo de IA fue revisado y validado por los integrantes.

Referencia de citación de IA: https://bibliotecas.duoc.cl/ia

## 8. Referencias

- Spring Boot y Actuator — https://docs.spring.io/spring-boot/
- Micrometer / Prometheus — https://micrometer.io/ , https://prometheus.io/
- Grafana — https://grafana.com/docs/
- Amazon EKS — https://docs.aws.amazon.com/eks/
- Amazon ECR — https://docs.aws.amazon.com/ecr/
- SonarCloud — https://docs.sonarsource.com/sonarcloud/
- Trivy — https://trivy.dev/
- Istio — https://istio.io/latest/docs/
- AWS CloudWatch — https://docs.aws.amazon.com/cloudwatch/
- GitHub Actions — https://docs.github.com/actions
