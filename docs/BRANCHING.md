# Modelo de ramificación y control de versiones (IE1 · IE3 · IE5)

Este proyecto usa un flujo **GitHub Flow extendido** con una rama de integración
(`develop`), pensado para trabajo colaborativo en la nube con trazabilidad completa.

## Ramas

| Rama | Propósito | Se fusiona a |
|------|-----------|--------------|
| `main` | Rama **principal y protegida**. Siempre desplegable; cada merge a `main` dispara el despliegue automático a producción (EKS). | — |
| `develop` | Rama de **integración**. Reúne las características terminadas antes de llevarlas a `main`. | `main` (vía PR) |
| `feature/<nombre>` | Nueva **característica**. Rama temporal creada desde `develop`. | `develop` (vía PR) |
| `hotfix/<nombre>` | **Corrección urgente** de un problema en producción. Se crea desde `main`. | `main` y `develop` (vía PR) |

## Reglas de colaboración

- **Todo cambio entra por Pull Request** — nunca se hace push directo a `main`.
- `main` tiene **branch protection**: requiere PR aprobado y que pasen los checks
  obligatorios: *Build/Test/Cobertura*, *Trivy*, *Pruebas de aceptación* y
  *SonarCloud*. Si alguno falla, la fusión queda bloqueada (IE6/IE9).
- Los commits describen el cambio con un prefijo (`feat`, `fix`, `docs`, `ci`, `deps`).
- Los tags marcan versiones desplegadas (p. ej. `v1.0.0`, `v2.0.0`) para trazabilidad.

## Ejemplo del flujo (trazabilidad)

```bash
# Nueva característica
git checkout develop
git checkout -b feature/endpoint-version
# ... cambios ...
git commit -m "feat: endpoint /api/version"
git push -u origin feature/endpoint-version
# -> Pull Request feature/endpoint-version -> develop -> (checks) -> merge

# Corrección urgente en producción
git checkout main
git checkout -b hotfix/fix-critico
git commit -m "fix: corrige problema crítico en producción"
git push -u origin hotfix/fix-critico
# -> Pull Request hotfix/fix-critico -> main -> (checks) -> merge -> despliegue automático
```

> El historial del repositorio (ramas `fase-*`, `feature-*`, `demo-ie6-falla-critica`
> y los Pull Requests #1 en adelante) evidencia este flujo aplicado durante el semestre.
