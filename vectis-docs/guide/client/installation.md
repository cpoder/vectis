# Installation du Client Vectis

## Options de déploiement

| Mode | Description | Recommandé pour |
|------|-------------|-----------------|
| Docker | Container autonome | Tests, petites installations |
| Docker Compose | Avec PostgreSQL | Production simple |
| Kubernetes | Helm chart | Production, haute disponibilité |
| JAR | Exécution directe | Développement |

## Docker (recommandé)

### Démarrage rapide

```bash
docker run -d \
  --name vectis-client \
  -p 9081:9081 \
  -v vectis-data:/data \
  ghcr.io/cpoder/vectis-client:latest
```

### Avec PostgreSQL

```bash
docker run -d \
  --name vectis-client \
  -p 9081:9081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/vectis \
  -e SPRING_DATASOURCE_USERNAME=vectis \
  -e SPRING_DATASOURCE_PASSWORD=vectis \
  ghcr.io/cpoder/vectis-client:latest
```

## Docker Compose

Créez un fichier `docker-compose.yml` :

```yaml
version: '3.8'

services:
  vectis-client:
    image: ghcr.io/cpoder/vectis-client:latest
    ports:
      - "9081:9081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/vectis
      SPRING_DATASOURCE_USERNAME: vectis
      SPRING_DATASOURCE_PASSWORD: vectis
    depends_on:
      - postgres
    volumes:
      - client-data:/data

  vectis-client-ui:
    image: ghcr.io/cpoder/vectis-client-ui:latest
    ports:
      - "3001:80"
    environment:
      VITE_API_URL: http://localhost:9081

  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: vectis
      POSTGRES_USER: vectis
      POSTGRES_PASSWORD: vectis
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  client-data:
  postgres-data:
```

Lancez avec :

```bash
docker-compose up -d
```

## Kubernetes (Helm)

```bash
# Ajouter le repo Helm
helm repo add vectis https://cpoder.github.io/vectis-helm-charts

# Installer le client
helm install vectis-client vectis/vectis-client \
  --namespace vectis \
  --create-namespace \
  --set postgresql.enabled=true
```

## JAR (développement)

### Prérequis

- Java 21+
- Maven 3.9+
- PostgreSQL

### Build

```bash
git clone https://github.com/cpoder/vectis-client.git
cd vectis-client
mvn package -DskipTests
```

### Exécution

```bash
java -jar target/vectis-client-1.0.0-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/vectis \
  --spring.datasource.username=vectis \
  --spring.datasource.password=vectis
```

## Vérification

Une fois démarré, vérifiez que le service fonctionne :

```bash
# Health check
curl http://localhost:9081/actuator/health

# Réponse attendue
{"status":"UP"}
```

L'interface web est accessible sur :
- API : http://localhost:9081
- UI : http://localhost:3001 (si déployée séparément)
- Swagger : http://localhost:9081/swagger-ui.html

## Prochaines étapes

- [Configuration](/guide/client/configuration)
- [Utilisation](/guide/client/usage)
