PROFILE ?= dev
BUILD_FLAGS ?=
COMPOSE ?= docker compose
COMPOSE_CMD = DOCKER_BUILDKIT=1 $(COMPOSE) $(if $(PROFILE),--profile $(PROFILE))

.PHONY: help env up build rebuild down all logs clean ps prune

help:
	@echo "Usage: make [target] PROFILE=dev|prod|gpu"
	@echo "Targets: env, build, rebuild, up, down, clean, logs, ps, prune"

env:
	@test -f .env || cp env.template .env

build: env
	$(COMPOSE_CMD) build $(BUILD_FLAGS)

rebuild: env
	$(COMPOSE_CMD) build --no-cache $(BUILD_FLAGS)

up: env
	$(COMPOSE_CMD) up --build -d

down: env
	$(COMPOSE_CMD) down

all: env clean up

logs:
	$(COMPOSE_CMD) logs -f

clean:
	$(COMPOSE_CMD) down -v --remove-orphans

ps:
	while true; do clear; $(COMPOSE_CMD) ps; sleep 3; done

prune:
	docker system prune -f
	docker volume prune -f || true
