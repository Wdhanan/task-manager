# frontend/taskmanager-frontend/Dockerfile

# ═══════════════════════════════════════════
# STUFE 1: BUILD (Angular kompilieren)
# ═══════════════════════════════════════════
FROM node:20-alpine AS build
# ↑ "Nimm das offizielle Node.js Image (Alpine = kleine Linux-Version)"
# "AS build" = Name dieser Stufe

WORKDIR /app
# ↑ "Wechsle in den /app Ordner im Container" (wird erstellt wenn nicht vorhanden)

# Erst nur package.json kopieren (Cache-Optimierung!)
# Wenn sich package.json nicht ändert, wird npm install nicht neu ausgeführt
COPY package*.json ./

# Dependencies installieren
RUN npm ci
# ↑ "npm ci" = Wie npm install, aber schneller und für CI/CD gedacht

# Den restlichen Quellcode kopieren
COPY . .

# Angular App bauen (für Produktion)
RUN npm run build -- --configuration=production
# ↑ Erstellt den "dist/" Ordner mit dem fertigen, optimierten HTML/JS/CSS

# ═══════════════════════════════════════════
# STUFE 2: SERVE (Nginx als Webserver)
# ═══════════════════════════════════════════
FROM nginx:alpine
# ↑ Nginx = Sehr schneller, leichter Webserver
# Wir brauchen Node.js nicht mehr – nur die fertigen Dateien

# Gebaute Dateien von Stufe 1 kopieren
COPY --from=build /app/dist/taskmanager-frontend/browser /usr/share/nginx/html
#                 ↑ Aus Stufe "build"

# Nginx Konfiguration kopieren (kommt gleich)
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
# ↑ "Dieser Container hört auf Port 80"

CMD ["nginx", "-g", "daemon off;"]
# ↑ Nginx im Vordergrund starten