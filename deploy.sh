#!/bin/bash
# ============================================
# deploy.sh — Roamaya Platform Deployment Script
# Usage:
#   First time:  ./deploy.sh init
#   Update:      ./deploy.sh update
#   Status:      ./deploy.sh status
#   Logs:        ./deploy.sh logs [service]
#   Down:        ./deploy.sh down
# ============================================

set -e

COMPOSE_FILE="docker-compose.yml"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() { echo -e "${GREEN}[DEPLOY]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# Check prerequisites
check_env() {
    if [ ! -f .env ]; then
        error ".env file not found! Copy .env.production to .env and fill in real values."
    fi

    # Validate critical vars are set
    source .env
    [ -z "$JWT_SECRET" ] && error "JWT_SECRET is not set in .env"
    [ -z "$DB_PASSWORD" ] && error "DB_PASSWORD is not set in .env"
    [ -z "$RAZORPAY_KEY" ] && error "RAZORPAY_KEY is not set in .env"

    log "Environment validated ✓"
}

case "${1:-}" in
    init)
        log "=== First-time deployment ==="
        check_env

        log "Building images..."
        docker compose build --no-cache

        log "Starting services..."
        docker compose --profile prod up -d

        log "Waiting for services to be healthy..."
        sleep 10
        docker compose ps

        log ""
        log "=== Next steps ==="
        log "1. Point your DNS (roamaya.in, api.roamaya.in) to this server"
        log "2. Run: ./init-ssl.sh to obtain SSL certificates"
        log "3. Verify: curl https://roamaya.in"
        ;;

    update)
        log "=== Rolling update ==="
        check_env

        log "Pulling latest code and rebuilding..."
        docker compose build

        log "Restarting services (zero-downtime)..."
        docker compose up -d --remove-orphans

        log "Waiting for health checks..."
        sleep 15
        docker compose ps

        log "Update complete ✓"
        ;;

    status)
        docker compose ps
        echo ""
        log "Service health:"
        docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
        ;;

    logs)
        SERVICE=${2:-""}
        if [ -n "$SERVICE" ]; then
            docker compose logs -f --tail=100 "$SERVICE"
        else
            docker compose logs -f --tail=50
        fi
        ;;

    down)
        warn "Stopping all services..."
        docker compose --profile prod down
        log "All services stopped."
        ;;

    *)
        echo "Usage: $0 {init|update|status|logs [service]|down}"
        echo ""
        echo "Commands:"
        echo "  init    - First-time setup (build + start + SSL instructions)"
        echo "  update  - Rebuild and restart services"
        echo "  status  - Show service status"
        echo "  logs    - Stream logs (optionally for a specific service)"
        echo "  down    - Stop all services"
        exit 1
        ;;
esac
