# 🎬 Guion del video — Despliegue automático a la nube (EP3)

Duración sugerida: **8–12 minutos**. Graba tu pantalla (con audio explicando).

---

## ✅ PASO 0 — Antes de grabar (preparación, NO se graba)

1. **Inicia el laboratorio de AWS** (Start Lab, punto verde 🟢).
2. **Renueva las credenciales de AWS:**
   - Copia el bloque de "AWS Details → AWS CLI".
   - Pégalo en `/Users/Marco-Parra/.aws/credentials` (reemplaza todo).
3. **Sincroniza los secretos de GitHub:**
   ```bash
   ./scripts/sync-aws-secrets.sh
   ```
4. **Verifica que el cluster esté arriba:**
   ```bash
   aws eks update-kubeconfig --name ep3-cluster --region us-east-1
   kubectl get nodes
   kubectl get pods -n devops-ep3
   ```
   - Si el cluster **NO existe** (el lab lo borró al cerrar sesión), recréalo (~20 min):
     ```bash
     eksctl create cluster -f k8s/eks-cluster.yaml
     kubectl apply -f k8s/01-namespace.yaml -f k8s/02-deployment.yaml \
                   -f k8s/03-service.yaml -f k8s/04-prometheus.yaml
     ```
5. **Obtén la URL pública** y déjala a mano:
   ```bash
   kubectl get svc -n devops-ep3
   ```

---

## 🎥 GUION (esto sí se graba)

### 1. Presentación (1 min)
- "Somos [nombres], y este es el encargo de Observabilidad y DevOps."
- Muestra el **repositorio en GitHub** y explica brevemente la arquitectura (abre el `README.md`).

### 2. Estado ANTES del cambio (1 min)
- Abre en el navegador la **URL de AWS**:
  `http://<URL-LoadBalancer>/api/version`
- Muestra que responde con `"version": "1.0.0"`.
- Di: "Este es el microservicio corriendo en AWS EKS. Vamos a cambiar la versión y verán cómo se despliega solo."

### 3. Hacer un cambio y push (2 min)
- Abre `src/main/java/cl/duoc/devops/InfoController.java`.
- Cambia `APP_VERSION = "1.0.0"` por `APP_VERSION = "2.0.0"`.
- Explica que trabajarás con una rama y un Pull Request (buena práctica + branch protection):
  ```bash
  git checkout -b demo-video
  git add -A
  git commit -m "demo: cambiar version a 2.0.0"
  git push -u origin demo-video
  gh pr create --fill
  ```

### 4. Ejecución del pipeline CI/CD (2–3 min)
- Abre la pestaña **Actions** en GitHub (o el PR).
- Explica cada check mientras corre:
  - **Build, Test y Cobertura** (compila, prueba, valida cobertura ≥70%).
  - **Análisis de seguridad (Trivy)** (busca vulnerabilidades).
  - **SonarCloud Code Analysis** (calidad de código).
- Muestra que **la fusión está bloqueada hasta que todos pasen** (branch protection → IE5/IE6).
- Cuando pasen, **fusiona el PR**:
  ```bash
  gh pr merge --squash
  ```

### 5. Despliegue AUTOMÁTICO en AWS (2 min)
- Al fusionar, se dispara el job **"Desplegar en AWS EKS"**. Ábrelo en Actions y explica:
  - Construye la imagen.
  - La publica en **Amazon ECR**.
  - Actualiza el despliegue en **EKS** (`kubectl set image` + `rollout`).
- Muestra en paralelo (opcional) el rollout:
  ```bash
  kubectl rollout status deployment/observability-service -n devops-ep3
  kubectl get pods -n devops-ep3
  ```

### 6. Estado DESPUÉS del cambio (1 min)
- Recarga en el navegador `http://<URL-LoadBalancer>/api/version`.
- Muestra que **ahora responde `"version": "2.0.0"`** — sin haber tocado el servidor manualmente.
- Di: "El cambio se publicó automáticamente en la nube gracias al pipeline CI/CD."

### 7. Observabilidad (1–2 min)
- Muestra el **dashboard de Grafana** con las métricas (CPU, memoria, errores, cobertura, disponibilidad).
- Muestra **Prometheus** en el cluster descubriendo los pods (`/targets`).
- (Opcional) Muestra el **dashboard de SonarCloud**.

### 8. Cierre (30 seg)
- Resume: "El pipeline integra pruebas, calidad, seguridad y despliegue automático, con observabilidad y políticas de cumplimiento."
- Recuerda mencionar la **URL del repositorio** para la entrega.

---

## 🧹 Después de grabar
- Borra el cluster para no gastar presupuesto:
  ```bash
  eksctl delete cluster --name ep3-cluster --region us-east-1
  ```

## 📤 Entrega (jueves 2 de julio, 23:59)
- **URL del repositorio:** https://github.com/Marco-Parra25/ep3-devops-observabilidad
- **Video** subido según indique el docente (AVA / enlace).
- (Opcional) capturas y evidencias adicionales.
- No olvides completar `docs/INFORME.md` (justificaciones, conclusiones y reflexiones **sin IA**).
