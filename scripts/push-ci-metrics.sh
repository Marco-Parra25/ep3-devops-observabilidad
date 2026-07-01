#!/usr/bin/env bash
# Empuja métricas del pipeline CI/CD al Pushgateway de Prometheus.
# Extrae la cobertura desde el reporte JaCoCo y registra el tiempo de despliegue.
#
# Uso:
#   ./scripts/push-ci-metrics.sh <PUSHGATEWAY_URL> <DEPLOY_SECONDS>
# Ejemplo local:
#   ./scripts/push-ci-metrics.sh http://localhost:9091 42
set -euo pipefail

PUSHGATEWAY_URL="${1:-http://localhost:9091}"
DEPLOY_SECONDS="${2:-0}"
JACOCO_XML="target/site/jacoco/jacoco.xml"

if [[ ! -f "$JACOCO_XML" ]]; then
  echo "No se encontró $JACOCO_XML. Ejecuta 'mvn verify' primero." >&2
  exit 1
fi

# Calcula la cobertura de líneas (covered / (covered + missed)) desde el XML de JaCoCo.
COVERAGE=$(python3 - "$JACOCO_XML" <<'PY'
import sys, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
covered = missed = 0
for c in root.findall('counter'):
    if c.get('type') == 'LINE':
        covered = int(c.get('covered')); missed = int(c.get('missed'))
total = covered + missed
print(f"{covered/total:.4f}" if total else "0")
PY
)

echo "Cobertura calculada: $COVERAGE   Tiempo de despliegue: ${DEPLOY_SECONDS}s"

# Empuja las métricas al Pushgateway (job=ci-pipeline).
cat <<EOF | curl -s --data-binary @- "${PUSHGATEWAY_URL}/metrics/job/ci-pipeline"
# TYPE ci_test_coverage_ratio gauge
# HELP ci_test_coverage_ratio Cobertura de pruebas del último build (0-1).
ci_test_coverage_ratio ${COVERAGE}
# TYPE ci_deployment_duration_seconds gauge
# HELP ci_deployment_duration_seconds Duración del último build/despliegue en segundos.
ci_deployment_duration_seconds ${DEPLOY_SECONDS}
EOF

echo "Métricas empujadas a ${PUSHGATEWAY_URL}"
