#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${REPO_ROOT}"

if [[ -e .local/config ]]; then
  echo "Каталог .local/config уже существует; сохраните настройки перед повторной инициализацией." >&2
  exit 1
fi

./scripts/compose.sh build bot

mkdir -p .local/config

TMP_CONTAINER_ID="$(docker create challenge-bot:local)"
cleanup() {
  docker rm -f "${TMP_CONTAINER_ID}" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker cp "${TMP_CONTAINER_ID}:/app/default-config/." "${REPO_ROOT}/.local/config"

echo "Конфиги выгружены в ${REPO_ROOT}/.local/config"
