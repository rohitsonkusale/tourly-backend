#!/bin/bash
# ============================================
# init-ssl.sh — Initialize Let's Encrypt SSL certificates
# Run ONCE before first production deploy.
# Usage: ./init-ssl.sh roamaya.in www.roamaya.in api.roamaya.in
# ============================================

set -e

DOMAINS=${@:-"roamaya.in www.roamaya.in api.roamaya.in"}
EMAIL="admin@roamaya.in"
DATA_PATH="./certbot"
STAGING=0  # Set to 1 for testing (avoids rate limits)

echo "=== Roamaya SSL Certificate Initialization ==="
echo "Domains: $DOMAINS"
echo "Email: $EMAIL"
echo ""

# Create required directories
mkdir -p "$DATA_PATH/conf"
mkdir -p "$DATA_PATH/www"

# Download TLS parameters if they don't exist
if [ ! -e "$DATA_PATH/conf/options-ssl-nginx.conf" ]; then
    echo "Downloading recommended TLS parameters..."
    curl -s https://raw.githubusercontent.com/certbot/certbot/master/certbot-nginx/certbot_nginx/_internal/tls_configs/options-ssl-nginx.conf > "$DATA_PATH/conf/options-ssl-nginx.conf"
    curl -s https://raw.githubusercontent.com/certbot/certbot/master/certbot/certbot/ssl-dhparams.pem > "$DATA_PATH/conf/ssl-dhparams.pem"
fi

# Create dummy certificate for nginx to start
echo "Creating dummy certificate..."
CERT_PATH="/etc/letsencrypt/live/roamaya.in"
mkdir -p "$DATA_PATH/conf/live/roamaya.in"
docker compose run --rm --entrypoint "\
    openssl req -x509 -nodes -newkey rsa:4096 -days 1 \
    -keyout '$CERT_PATH/privkey.pem' \
    -out '$CERT_PATH/fullchain.pem' \
    -subj '/CN=localhost'" certbot

echo "Starting nginx with dummy cert..."
docker compose up -d nginx

echo "Deleting dummy certificate..."
docker compose run --rm --entrypoint "\
    rm -Rf /etc/letsencrypt/live/roamaya.in && \
    rm -Rf /etc/letsencrypt/archive/roamaya.in && \
    rm -Rf /etc/letsencrypt/renewal/roamaya.in.conf" certbot

echo "Requesting real certificate from Let's Encrypt..."

# Build domain args
DOMAIN_ARGS=""
for domain in $DOMAINS; do
    DOMAIN_ARGS="$DOMAIN_ARGS -d $domain"
done

# Staging flag
STAGING_ARG=""
if [ $STAGING -eq 1 ]; then
    STAGING_ARG="--staging"
fi

docker compose run --rm --entrypoint "\
    certbot certonly --webroot -w /var/www/certbot \
    $STAGING_ARG \
    --email $EMAIL \
    --rsa-key-size 4096 \
    --agree-tos \
    --no-eff-email \
    --force-renewal \
    $DOMAIN_ARGS" certbot

echo "Reloading nginx with real certificate..."
docker compose exec nginx nginx -s reload

echo ""
echo "=== SSL setup complete! ==="
echo "Certificates will auto-renew via the certbot container."
