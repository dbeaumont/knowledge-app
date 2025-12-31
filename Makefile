PROFILE ?= dev
BUILD_FLAGS ?=
COMPOSE_CMD = DOCKER_BUILDKIT=1 docker compose $(if $(PROFILE),--profile $(PROFILE))

# BuildKit est le moteur de build moderne de Docker. Il parallélise les étapes, met en cache 
# plus finement (y compris sur plusieurs architectures), supporte les secrets et mounts temporaires 
# pendant le build, et produit des images plus rapidement et de façon reproductible par rapport à 
# l’ancien backend docker build. On l’active via DOCKER_BUILDKIT=1 ou dans la config Docker.

.PHONY: help env up build down all logs clean ps prune

help:
	@echo "Usage : make [target] PROFILE=dev|prod|gpu BUILD_FLAGS=--no-cache"
	@echo "Targets: env, build, up, down, clean, logs, ps, prune"

env:
	@test -f .env || cp env.template .env

build: env
	$(COMPOSE_CMD) build $(BUILD_FLAGS)

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
