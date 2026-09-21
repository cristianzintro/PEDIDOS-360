#!/usr/bin/env bash

set -Eeuo pipefail

readonly SERVICE_USER="pedidos360"
readonly SERVICE_GROUP="pedidos360"
readonly ENV_DIR="/etc/pedidos360"
readonly SYSTEMD_DIR="/etc/systemd/system"

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly ENV_SOURCE_DIR="${SCRIPT_DIR}/env"
readonly SYSTEMD_SOURCE_DIR="${SCRIPT_DIR}/systemd"

readonly -a ENV_NAMES=(ot audit bff)
readonly -a SERVICES=(
  pedidos360-ot.service
  pedidos360-audit.service
  pedidos360-bff.service
)

enable_services=false
unit_paths=()

usage() {
  echo "Usage: sudo bash deploy/install-ec2.sh [--enable]"
}

case "${1:-}" in
  "") ;;
  --enable) enable_services=true ;;
  *)
    usage >&2
    exit 2
    ;;
esac

if (( $# > 1 )); then
  usage >&2
  exit 2
fi

if (( EUID != 0 )); then
  echo "This script must run as root." >&2
  exit 1
fi

for command_name in getent grep groupadd id install systemctl tr useradd usermod; do
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "Required command not found: ${command_name}" >&2
    exit 1
  fi
done

if ! getent group "${SERVICE_GROUP}" >/dev/null; then
  groupadd --system "${SERVICE_GROUP}"
fi

if ! id "${SERVICE_USER}" >/dev/null 2>&1; then
  useradd \
    --system \
    --gid "${SERVICE_GROUP}" \
    --home-dir /opt/pedidos360 \
    --shell /sbin/nologin \
    "${SERVICE_USER}"
elif ! id -nG "${SERVICE_USER}" | tr ' ' '\n' | grep -Fxq "${SERVICE_GROUP}"; then
  usermod --append --groups "${SERVICE_GROUP}" "${SERVICE_USER}"
fi

install -d -o root -g "${SERVICE_GROUP}" -m 0750 "${ENV_DIR}"

for env_name in "${ENV_NAMES[@]}"; do
  source_file="${ENV_SOURCE_DIR}/${env_name}.env.example"
  target_file="${ENV_DIR}/${env_name}.env"

  if [[ ! -f "${source_file}" ]]; then
    echo "Environment example not found: ${source_file}" >&2
    exit 1
  fi

  if [[ -e "${target_file}" ]]; then
    echo "Preserving existing environment file: ${target_file}"
  else
    install -o root -g "${SERVICE_GROUP}" -m 0640 "${source_file}" "${target_file}"
    echo "Created placeholder environment file: ${target_file}"
  fi
done

for service in "${SERVICES[@]}"; do
  source_file="${SYSTEMD_SOURCE_DIR}/${service}"

  if [[ ! -f "${source_file}" ]]; then
    echo "Systemd unit not found: ${source_file}" >&2
    exit 1
  fi

  target_file="${SYSTEMD_DIR}/${service}"
  install -o root -g root -m 0644 "${source_file}" "${target_file}"
  unit_paths+=("${target_file}")
done

if command -v systemd-analyze >/dev/null 2>&1; then
  systemd-analyze verify "${unit_paths[@]}"
fi

systemctl daemon-reload

if [[ "${enable_services}" == true ]]; then
  systemctl enable "${SERVICES[@]}"
fi

echo "Systemd units installed."
echo "Replace every placeholder in ${ENV_DIR}/*.env before starting the services."
if [[ "${enable_services}" == false ]]; then
  echo "Run this script again with --enable when the environment files are ready."
fi
