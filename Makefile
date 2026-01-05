COMPOSE        ?= docker compose
PROFILE        ?= dev
BUILD          ?=
BUILD_FLAGS    ?=
DEBUG          ?=
MODEL          ?= llama3.1:8b

DOCKER_BUILDKIT ?= 1
COMPOSE_CMD     := $(if $(DEBUG),DEBUG=$(DEBUG) ,) DOCKER_BUILDKIT=$(DOCKER_BUILDKIT) $(COMPOSE) $(if $(PROFILE),--profile $(PROFILE))

.PHONY: help env up build down all logs clean ps prune pull-model

help:
	@echo "Usage : make [target] PROFILE=dev|prod|gpu [BUILD=service] [BUILD_FLAGS=--no-cache] [DEBUG=true|false]"
	@echo "targets : env, build, up, down, clean, logs, ps, prune"
	@echo "service : rag-service | document-service | gateway | frontend | (empty=all)"
	@echo "pull-model : download Ollama model (MODEL=$(MODEL)) inside the ollama container"
	@echo "Examples:"
	@echo "make clean up PROFILE=dev"
	@echo "make build BUILD=gateway BUILD_FLAGS=--no-cache PROFILE=dev"
	@echo "make pull-model MODEL=llama3.1:8b PROFILE=dev"

env:
	@test -f .env || cp env.template .env

build: env
	$(COMPOSE_CMD) build $(BUILD_FLAGS) $(BUILD)

up: env
	$(COMPOSE_CMD) up -d

all: env clean build up

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

pull-model:
	$(COMPOSE_CMD) up -d ollama
	$(COMPOSE_CMD) exec ollama ollama pull $(MODEL)
