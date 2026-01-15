# Docker compose wrapper & project defaults.
COMPOSE         ?= docker compose
PROFILE         ?= dev
BUILD           ?=
BUILD_FLAGS     ?=
DEBUG           ?=
CHAT_MODEL      ?= qwen2.5
EMBEDDING_MODEL ?= mxbai-embed-large

# Local TLS / CA defaults (used by keycloak/certgen.sh).
KEYCLOAK_HOST   ?= keycloak.local
ROOT_CA_DIR ?= .certs
ROOT_CA_KEY ?= $(ROOT_CA_DIR)/ca.key
ROOT_CA_CERT ?= $(ROOT_CA_DIR)/ca.crt
ROOT_CA_TRUSTSTORE ?= $(ROOT_CA_DIR)/truststore.p12
ROOT_CA_TRUSTSTORE_PASS ?= changeit
ROOT_CA_DAYS ?= 3650

DOCKER_BUILDKIT ?= 1
COMPOSE_CMD     := $(if $(DEBUG),DEBUG=$(DEBUG) ,) DOCKER_BUILDKIT=$(DOCKER_BUILDKIT) $(COMPOSE) $(if $(PROFILE),--profile $(PROFILE))

.PHONY: help env up build down all logs clean ps prune pull-models run-models model-up hosts-keycloak ca-root ca-keycloak

help:
	@echo "Usage : make [target] PROFILE=dev|prod [BUILD=service] [BUILD_FLAGS=--no-cache] [DEBUG=true|false]"
	@echo "targets : env, build, up, down, clean, logs, ps, prune, all, pull-models, run-models"
	@echo "service : rag-service | document-service | gateway | frontend | (empty=all)"
	@echo "pull-models : download Docker model runner model (CHAT_MODEL=$(CHAT_MODEL) - EMBEDDING_MODEL=$(EMBEDDING_MODEL))"
	@echo "run-models : start Docker model runner chat model (CHAT_MODEL=$(CHAT_MODEL))"
	@echo "hosts-keycloak : ajoute '127.0.0.1 $(KEYCLOAK_HOST)' dans /etc/hosts (sudo)"
	@echo "ca-root : genere une CA root locale pour tout le projet (.certs/)"
	@echo "ca-keycloak : alias de ca-root"
	@echo "Examples:"
	@echo "make all"
	@echo "make build BUILD=gateway BUILD_FLAGS=--no-cache"
	@echo "make clean up PROFILE=prod"
	@echo "make pull-models CHAT_MODEL=qwen2.5 EMBEDDING_MODEL=mxbai-embed-large"

env:
	# Create .env from template when missing.
	@test -f .env || cp env.template .env

build: env
	# Build all services or a single one via BUILD=service.
	$(COMPOSE_CMD) build $(BUILD_FLAGS) $(BUILD)

up: env
	# Start the stack in the selected profile.
	$(COMPOSE_CMD) up -d

all: hosts-keycloak env clean build run-models up

down:
	$(COMPOSE_CMD) down

clean:
	# Full teardown, including volumes.
	$(COMPOSE_CMD) down -v --remove-orphans

logs:
	$(COMPOSE_CMD) logs -f

ps:
	while true; do clear; $(COMPOSE_CMD) ps; sleep 3; done

prune:
	# Host cleanup (not scoped to this project).
	docker system prune -f
	docker volume prune -f || true

pull-models:
	# Pull local model runner images.
	docker model pull $(CHAT_MODEL)
	docker model pull $(EMBEDDING_MODEL)

run-models:
	# Run the chat model; embeddings auto-load on first request.
	docker model run -d $(CHAT_MODEL)
	@echo "Embedding models are encoder-only; they auto-load on first /v1/embeddings request."

hosts-keycloak:
	# Add local DNS entry for Keycloak HTTPS (requires sudo).
	@grep -qE '^[[:space:]]*127\.0\.0\.1[[:space:]]+$(KEYCLOAK_HOST)([[:space:]]|$$)' /etc/hosts || \
		sudo sh -c 'echo "127.0.0.1 $(KEYCLOAK_HOST)" >> /etc/hosts'
	@grep -qE '^[[:space:]]*127\.0\.0\.1[[:space:]]+$(KEYCLOAK_HOST)([[:space:]]|$$)' /etc/hosts && \
		echo "OK: $(KEYCLOAK_HOST) present in /etc/hosts"

ca-root:
	# Generate a local root CA for HTTPS development.
	@ROOT_CA_DIR="$(ROOT_CA_DIR)" \
	ROOT_CA_KEY="$(ROOT_CA_KEY)" \
	ROOT_CA_CERT="$(ROOT_CA_CERT)" \
	ROOT_CA_DAYS="$(ROOT_CA_DAYS)" \
	ROOT_CA_TRUSTSTORE="$(ROOT_CA_TRUSTSTORE)" \
	ROOT_CA_TRUSTSTORE_PASS="$(ROOT_CA_TRUSTSTORE_PASS)" \
	./scripts/certgenrootca.sh
