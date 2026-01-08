COMPOSE         ?= docker compose
PROFILE         ?= dev
BUILD           ?=
BUILD_FLAGS     ?=
DEBUG           ?=
CHAT_MODEL      ?= qwen2.5
EMBEDDING_MODEL ?= mxbai-embed-large

DOCKER_BUILDKIT ?= 1
COMPOSE_CMD     := $(if $(DEBUG),DEBUG=$(DEBUG) ,) DOCKER_BUILDKIT=$(DOCKER_BUILDKIT) $(COMPOSE) $(if $(PROFILE),--profile $(PROFILE))

.PHONY: help env up build down all logs clean ps prune pull-models run-models model-up

help:
	@echo "Usage : make [target] PROFILE=dev|prod [BUILD=service] [BUILD_FLAGS=--no-cache] [DEBUG=true|false]"
	@echo "targets : env, build, up, down, clean, logs, ps, prune, all, pull-models, run-models"
	@echo "service : rag-service | document-service | gateway | frontend | (empty=all)"
	@echo "pull-models : download Docker model runner model (CHAT_MODEL=$(CHAT_MODEL) - EMBEDDING_MODEL=$(EMBEDDING_MODEL))"
	@echo "run-models : start Docker model runner chat model (CHAT_MODEL=$(CHAT_MODEL))"
	@echo "Examples:"
	@echo "make all"
	@echo "make build BUILD=gateway BUILD_FLAGS=--no-cache"
	@echo "make clean up PROFILE=prod"
	@echo "make pull-models CHAT_MODEL=qwen2.5 EMBEDDING_MODEL=mxbai-embed-large"

env:
	@test -f .env || cp env.template .env

build: env
	$(COMPOSE_CMD) build $(BUILD_FLAGS) $(BUILD)

up: env
	$(COMPOSE_CMD) up -d

all: env clean build run-models up

down:
	$(COMPOSE_CMD) down

clean:
	$(COMPOSE_CMD) down -v --remove-orphans

logs:
	$(COMPOSE_CMD) logs -f

ps:
	while true; do clear; $(COMPOSE_CMD) ps; sleep 3; done

prune:
	docker system prune -f
	docker volume prune -f || true

pull-models:
	docker model pull $(CHAT_MODEL)
	docker model pull $(EMBEDDING_MODEL)

run-models:
	docker model run -d $(CHAT_MODEL)
	@echo "Embedding models are encoder-only; they auto-load on first /v1/embeddings request."
