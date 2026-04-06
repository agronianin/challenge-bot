#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${REPO_ROOT}"

docker compose build bot

IMAGE_ID="$(docker compose images -q bot)"
if [[ -z "${IMAGE_ID}" ]]; then
  echo "Не удалось получить image id для сервиса bot" >&2
  exit 1
fi

mkdir -p config

TMP_CONTAINER_ID="$(docker create "${IMAGE_ID}")"
cleanup() {
  docker rm -f "${TMP_CONTAINER_ID}" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker cp "${TMP_CONTAINER_ID}:/app/default-config/." "${REPO_ROOT}/config"

echo "Конфиги выгружены в ${REPO_ROOT}/config"
