# AI Knowledge Workspace

Plateforme RAG full offline (Mac M1) : microservices Spring Boot + frontend Angular, LLM local via Docker Model Runner, Qdrant pour la recherche vectorielle.

## En bref
- Java 21 / Spring Boot 3.3.x, Spring Cloud 2023.0.x, Spring AI (OpenAI starter)
- RAG: Model Runner (qwen2.5, mxbai-embed-large) + Qdrant
- Data: PostgreSQL, JPA + MapStruct
- Frontend: Angular 18 (BFF OIDC via Gateway)
- Orchestration: Docker Compose (`dev` / `prod`)

Note : Spring AI le plus recent supporte uniquement Spring Boot 3.3.x et Spring Cloud 2023.0.x.

## Services
- `frontend` : Angular + Nginx, reverse-proxy `/api`, `/oauth2`, `/login`, `/logout`
- `gateway` : BFF Spring Cloud Gateway (OIDC, session, TokenRelay)
- `keycloak` + `keycloak-certgen` : IdP OIDC + generation de cert
- `document-service` : CRUD documents, extraction texte, PostgreSQL
- `rag-service` : ingestion, embeddings, Qdrant, chat
- `user-service` : profil utilisateur (`/api/users/me`)
- `postgres`, `qdrant`, `modelrunner`, `toolbox`

## Prerequis (Mac M1)
- Docker Desktop (+ Model Runner active et TCP host-side)
- ~12GB RAM pour les modeles
- Modeles precharges: `docker model pull qwen2.5` et `docker model pull mxbai-embed-large`
- JDK 21 + Maven 3.9 (si build hors Docker)
- Node 20 (si build frontend hors Docker)

## Demarrage rapide
Option automatique
```bash
make ca-root
# Importer la CA locale: docs/ca-import.md
make all
```

Option pas a pas
```bash
make ca-rootvscode, comment effectuer un merge
# Importer la CA locale: docs/ca-import.md
make env
make hosts-keycloak
make pull-models
make run-models
make build up
```

Endpoints
```bash
# Frontend : http://localhost:4200
# Gateway : http://localhost:8080
# Keycloak : https://keycloak.local:8443
# Qdrant : http://localhost:6333
# Model Runner : http://localhost:12434/v1
```

## Auth par defaut
- `admin/admin123` (roles ADMIN,USER)
- `user/user123` (role USER)

## API (exemples)
- Docs: `GET /api/documents`, `POST /api/documents` (multipart `file` ou `content`)
- RAG: `POST /api/rag/answer {query}`, streaming SSE `/api/rag/query`

## Build locaux (sans Docker)
```bash
cd backend && mvn clean package
cd frontend && npm install && npm run build
```

## Docs utiles
- `docs/ca-import.md` : import de la CA locale
- `docs/rag-call-flow.md` : flux RAG (ingestion + query)
