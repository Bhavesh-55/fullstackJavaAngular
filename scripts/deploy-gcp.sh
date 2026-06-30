#!/usr/bin/env bash

set -euo pipefail

# ==========================================================
# GCP Deployment Script for Angular + Spring Boot + MySQL
# Usage:
#   ./scripts/deploy-gcp.sh frontend
#   ./scripts/deploy-gcp.sh backend
#   ./scripts/deploy-gcp.sh all
# ==========================================================

DEPLOY_TARGET="${1:-frontend}"

# -------- GCP CONFIGURATION --------
PROJECT_ID="project-f689a914-3465-4c93-9ef"
REGION="us-central1"
ZONE="us-central1-a"
REPO_NAME="skill-dashboard-repo"
VM_NAME="skill-dashboard-vm"

# -------- PROJECT PATHS --------
BACKEND_DIR="./skill-dashboard-backend"
FRONTEND_DIR="./skill-dashboard-frontend"
COMPOSE_FILE="./docker-compose.gcp.yml"
ENV_FILE="./.env.gcp"

# -------- VALIDATE DEPLOY TARGET --------
if [[ "$DEPLOY_TARGET" != "frontend" && "$DEPLOY_TARGET" != "backend" && "$DEPLOY_TARGET" != "all" ]]; then
  echo "Invalid deploy target: $DEPLOY_TARGET"
  echo "Allowed values: frontend, backend, all"
  exit 1
fi

# -------- BASIC FILE CHECKS --------
if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing .env.gcp file at: $ENV_FILE"
  exit 1
fi

if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "Missing docker-compose.gcp.yml file at: $COMPOSE_FILE"
  exit 1
fi

if [[ ! -d "$BACKEND_DIR" ]]; then
  echo "Missing backend folder: $BACKEND_DIR"
  exit 1
fi

if [[ ! -d "$FRONTEND_DIR" ]]; then
  echo "Missing frontend folder: $FRONTEND_DIR"
  exit 1
fi

# -------- CREATE IMAGE TAG FROM GIT COMMIT --------
GIT_SHA="$(git rev-parse --short HEAD)"
TIMESTAMP="$(date +%Y%m%d%H%M%S)"
IMAGE_TAG="${GIT_SHA}-${TIMESTAMP}"

BACKEND_IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/skill-dashboard-backend:${IMAGE_TAG}"
FRONTEND_IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/skill-dashboard-frontend:${IMAGE_TAG}"

echo "=============================================="
echo "Deploy target: $DEPLOY_TARGET"
echo "Project ID:    $PROJECT_ID"
echo "Region:        $REGION"
echo "Zone:          $ZONE"
echo "VM Name:       $VM_NAME"
echo "Image tag:     $IMAGE_TAG"
echo "=============================================="

# -------- SET GCP PROJECT --------
gcloud config set project "$PROJECT_ID"

# -------- CONFIGURE DOCKER AUTH FOR ARTIFACT REGISTRY --------
gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet

# -------- FUNCTION TO UPDATE .env.gcp --------
update_env_var() {
  local key="$1"
  local value="$2"

  if grep -q "^${key}=" "$ENV_FILE"; then
    sed -i.bak "s|^${key}=.*|${key}=${value}|" "$ENV_FILE"
  else
    echo "${key}=${value}" >> "$ENV_FILE"
  fi
}

# -------- BUILD AND PUSH BACKEND --------
if [[ "$DEPLOY_TARGET" == "backend" || "$DEPLOY_TARGET" == "all" ]]; then
  echo "Building backend image..."
  docker build -t "$BACKEND_IMAGE" "$BACKEND_DIR"

  echo "Pushing backend image..."
  docker push "$BACKEND_IMAGE"

  echo "Updating BACKEND_IMAGE in .env.gcp..."
  update_env_var "BACKEND_IMAGE" "$BACKEND_IMAGE"
fi

# -------- BUILD AND PUSH FRONTEND --------
if [[ "$DEPLOY_TARGET" == "frontend" || "$DEPLOY_TARGET" == "all" ]]; then
  echo "Building frontend image..."
  docker build -t "$FRONTEND_IMAGE" "$FRONTEND_DIR"

  echo "Pushing frontend image..."
  docker push "$FRONTEND_IMAGE"

  echo "Updating FRONTEND_IMAGE in .env.gcp..."
  update_env_var "FRONTEND_IMAGE" "$FRONTEND_IMAGE"
fi

# -------- COPY UPDATED FILES TO VM --------
echo "Copying docker-compose.gcp.yml to VM..."
gcloud compute scp "$COMPOSE_FILE" "${VM_NAME}:/home/Bhavesh/docker-compose.gcp.yml" --zone="$ZONE" --quiet

echo "Copying .env.gcp to VM..."
gcloud compute scp "$ENV_FILE" "${VM_NAME}:/home/Bhavesh/.env.gcp" --zone="$ZONE" --quiet

# -------- DEPLOY ON VM --------
echo "Deploying on VM..."

if [[ "$DEPLOY_TARGET" == "frontend" ]]; then
  gcloud compute ssh "$VM_NAME" --zone="$ZONE" --quiet --command="
    set -e
    docker compose --env-file .env.gcp -f docker-compose.gcp.yml pull angular-app
    docker compose --env-file .env.gcp -f docker-compose.gcp.yml up -d --no-deps angular-app
    docker image prune -f
    docker ps
  "
fi

if [[ "$DEPLOY_TARGET" == "backend" ]]; then
  gcloud compute ssh "$VM_NAME" --zone="$ZONE" --quiet --command="
    set -e
    docker compose --env-file .env.gcp -f docker-compose.gcp.yml pull springboot-app
    docker compose --env-file .env.gcp -f docker-compose.gcp.yml up -d --no-deps springboot-app
    docker image prune -f
    docker ps
  "
fi

if [[ "$DEPLOY_TARGET" == "all" ]]; then
  gcloud compute ssh "$VM_NAME" --zone="$ZONE" --quiet --command="
    set -e
    docker compose --env-file .env.gcp -f docker-compose.gcp.yml pull
    docker compose --env-file .env.gcp -f docker-compose.gcp.yml up -d
    docker image prune -f
    docker ps
  "
fi

echo "=============================================="
echo "Deployment completed successfully."
echo "Target: $DEPLOY_TARGET"
echo "Tag:    $IMAGE_TAG"
echo "=============================================="