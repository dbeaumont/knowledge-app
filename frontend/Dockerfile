# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm config set fund false \
 && npm config set audit false \
 && npm install --prefer-offline --no-audit --progress=false --ignore-optional
COPY . .
RUN npm run build --if-present

# Runtime stage
FROM nginx:1.25-alpine
COPY --from=build /app/dist/app /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
