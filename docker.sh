#!/bin/bash
# ============================================================================
# TicketWave Docker Compose Management Script
# ============================================================================
# Usage: ./docker.sh [command]
# Commands:
#   up          - Start all services
#   down        - Stop all services
#   logs        - Show logs from all services
#   ps          - Show running services status
#   build       - Build Docker images
#   clean       - Remove all containers and volumes
#   restart     - Restart all services
#   healthcheck - Check health of all services
#   help        - Show this help message
# ============================================================================

set -e

DOCKER_COMPOSE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/infra"
DOCKER_COMPOSE_FILE="${DOCKER_COMPOSE_DIR}/docker-compose.yml"
ENV_FILE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/.env"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

function log_info() {
  echo -e "${BLUE}[INFO]${NC} $1"
}

function log_success() {
  echo -e "${GREEN}[✓]${NC} $1"
}

function log_error() {
  echo -e "${RED}[✗]${NC} $1"
}

function log_warning() {
  echo -e "${YELLOW}[WARN]${NC} $1"
}

function check_docker() {
  if ! command -v docker &> /dev/null; then
    log_error "Docker is not installed or not in PATH"
    exit 1
  fi
  if ! command -v docker-compose &> /dev/null; then
    log_error "Docker Compose is not installed or not in PATH"
    exit 1
  fi
  log_success "Docker and Docker Compose are available"
}

function check_env_file() {
  if [ ! -f "$ENV_FILE" ]; then
    log_warning ".env file not found, creating from .env.example"
    if [ -f "${ENV_FILE}.example" ]; then
      cp "${ENV_FILE}.example" "$ENV_FILE"
      log_success "Created .env from .env.example"
    else
      log_error ".env.example not found"
      exit 1
    fi
  fi
}

function cmd_up() {
  log_info "Starting TicketWave services..."
  check_env_file
  cd "$DOCKER_COMPOSE_DIR"
  docker-compose -f "$DOCKER_COMPOSE_FILE" up -d
  log_success "Services started successfully"
  log_info "Waiting for services to be healthy..."
  sleep 5
  cmd_ps
}

function cmd_down() {
  log_info "Stopping TicketWave services..."
  cd "$DOCKER_COMPOSE_DIR"
  docker-compose -f "$DOCKER_COMPOSE_FILE" down
  log_success "Services stopped"
}

function cmd_logs() {
  log_info "Showing logs (press Ctrl+C to exit)..."
  cd "$DOCKER_COMPOSE_DIR"
  docker-compose -f "$DOCKER_COMPOSE_FILE" logs -f
}

function cmd_ps() {
  log_info "TicketWave Services Status:"
  cd "$DOCKER_COMPOSE_DIR"
  docker-compose -f "$DOCKER_COMPOSE_FILE" ps
}

function cmd_build() {
  log_info "Building Docker images..."
  cd "$DOCKER_COMPOSE_DIR"
  docker-compose -f "$DOCKER_COMPOSE_FILE" build
  log_success "Build completed"
}

function cmd_clean() {
  log_warning "This will remove all containers, volumes, and networks related to TicketWave"
  read -p "Are you sure? (y/N): " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd "$DOCKER_COMPOSE_DIR"
    docker-compose -f "$DOCKER_COMPOSE_FILE" down -v --remove-orphans
    log_success "Cleanup completed"
  else
    log_info "Cleanup cancelled"
  fi
}

function cmd_restart() {
  log_info "Restarting all services..."
  cmd_down
  sleep 2
  cmd_up
}

function cmd_healthcheck() {
  log_info "Checking health of all services..."
  cd "$DOCKER_COMPOSE_DIR"
  
  echo ""
  log_info "Checking PostgreSQL..."
  if docker-compose -f "$DOCKER_COMPOSE_FILE" exec -T postgres pg_isready -U postgres &> /dev/null; then
    log_success "PostgreSQL is healthy"
  else
    log_error "PostgreSQL is not responding"
  fi
  
  echo ""
  log_info "Checking Redis..."
  if docker-compose -f "$DOCKER_COMPOSE_FILE" exec -T redis redis-cli ping &> /dev/null; then
    log_success "Redis is healthy"
  else
    log_error "Redis is not responding"
  fi
  
  echo ""
  log_info "Checking RabbitMQ..."
  if docker-compose -f "$DOCKER_COMPOSE_FILE" exec -T rabbitmq rabbitmq-diagnostics ping &> /dev/null; then
    log_success "RabbitMQ is healthy"
  else
    log_error "RabbitMQ is not responding"
  fi
  
  echo ""
  log_info "Checking LocalStack..."
  if docker-compose -f "$DOCKER_COMPOSE_FILE" exec -T localstack awslocal s3 ls &> /dev/null; then
    log_success "LocalStack is healthy"
  else
    log_error "LocalStack is not responding"
  fi
  
  echo ""
  log_info "Checking Application..."
  if docker-compose -f "$DOCKER_COMPOSE_FILE" exec -T app curl -f http://localhost:8080/actuator/health &> /dev/null; then
    log_success "Application is healthy"
  else
    log_error "Application is not responding"
  fi
}

function cmd_help() {
  cat << EOF
Usage: ./docker.sh [command]

Commands:
  up              Start all services in detached mode
  down            Stop all services
  logs            Show logs from all services (follow mode)
  ps              Show running services status
  build           Build Docker images
  clean           Remove containers, volumes, and networks
  restart         Restart all services
  healthcheck     Check health of all services
  help            Show this help message

Examples:
  ./docker.sh up
  ./docker.sh logs
  ./docker.sh down
  ./docker.sh clean

For more information, see the documentation in docs/
EOF
}

# Main script logic
check_docker

case "${1:-help}" in
  up)
    cmd_up
    ;;
  down)
    cmd_down
    ;;
  logs)
    cmd_logs
    ;;
  ps)
    cmd_ps
    ;;
  build)
    cmd_build
    ;;
  clean)
    cmd_clean
    ;;
  restart)
    cmd_restart
    ;;
  healthcheck)
    cmd_healthcheck
    ;;
  help|--help|-h)
    cmd_help
    ;;
  *)
    log_error "Unknown command: $1"
    cmd_help
    exit 1
    ;;
esac
