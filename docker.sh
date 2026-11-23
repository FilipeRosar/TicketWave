#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

case "${1:-help}" in
  up) docker compose up -d --wait ;;
  down|ps|build) docker compose "$1" ;;
  logs) docker compose logs -f ;;
  restart)
    docker compose down
    docker compose up -d --wait
    ;;
  clean)
    read -r -p "Remover os contêineres e volumes do TicketWave? [s/N] " answer
    if [[ "$answer" == [sSyY] ]]; then
      docker compose down -v --remove-orphans
    fi
    ;;
  healthcheck) docker compose ps ;;
  help|--help|-h) echo "Uso: ./docker.sh {up|down|logs|ps|build|clean|restart|healthcheck}" ;;
  *) echo "Comando desconhecido: $1" >&2; exit 1 ;;
esac
