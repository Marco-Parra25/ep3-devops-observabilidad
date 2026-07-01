#!/usr/bin/env bash
# Sincroniza las credenciales de AWS (de ~/.aws/credentials) hacia los
# secretos de GitHub que usa el pipeline para desplegar en EKS.
#
# Las credenciales de AWS Academy Learner Lab EXPIRAN cada sesión.
# Ejecuta este script cada vez que renueves las credenciales (antes de grabar el video):
#   1) Pega las credenciales nuevas del lab en ~/.aws/credentials
#   2) Ejecuta:  ./scripts/sync-aws-secrets.sh
set -euo pipefail

AKI=$(aws configure get aws_access_key_id)
ASK=$(aws configure get aws_secret_access_key)
AST=$(aws configure get aws_session_token)

if [[ -z "$AKI" || -z "$ASK" ]]; then
  echo "No se encontraron credenciales en ~/.aws/credentials" >&2
  exit 1
fi

printf '%s' "$AKI" | gh secret set AWS_ACCESS_KEY_ID
printf '%s' "$ASK" | gh secret set AWS_SECRET_ACCESS_KEY
printf '%s' "$AST" | gh secret set AWS_SESSION_TOKEN

echo "✅ Secretos de AWS actualizados en GitHub."
echo "   Verificando identidad AWS:"
aws sts get-caller-identity --query 'Arn' --output text
