# Authentification

## Méthodes supportées

| API | Méthode | Usage |
|-----|---------|-------|
| Client API | Bearer Token / API Key | Production |
| Admin API | Basic Auth | Administration |
| Server API | Basic Auth | Configuration |

## Basic Authentication

Pour les APIs Admin et Server :

```bash
curl -u admin:password http://localhost:9080/api/v1/clusters
```

Ou avec header :
```bash
curl -H "Authorization: Basic YWRtaW46cGFzc3dvcmQ=" http://localhost:9080/api/v1/clusters
```

## API Key (Client API)

### Générer une clé API

```bash
curl -X POST http://localhost:9081/api/auth/api-keys \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Integration ERP",
    "expiresAt": "2026-01-01T00:00:00Z"
  }'
```

Réponse :
```json
{
  "id": "ak_123456789",
  "key": "pk_live_abc123def456...",
  "name": "Integration ERP",
  "createdAt": "2025-01-10T10:00:00Z",
  "expiresAt": "2026-01-01T00:00:00Z"
}
```

::: warning
La clé complète n'est affichée qu'une seule fois. Conservez-la en lieu sûr.
:::

### Utiliser la clé API

```bash
curl -H "X-API-Key: pk_live_abc123def456..." \
  http://localhost:9081/api/transfers
```

Ou via query parameter :
```bash
curl "http://localhost:9081/api/transfers?api_key=pk_live_abc123def456..."
```

### Révoquer une clé

```bash
curl -X DELETE http://localhost:9081/api/auth/api-keys/ak_123456789 \
  -u admin:admin
```

## JWT (optionnel)

Pour les intégrations OAuth2/OIDC :

### Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.monentreprise.com/realms/vectis
```

### Utilisation

```bash
# Obtenir un token (exemple Keycloak)
TOKEN=$(curl -X POST https://auth.monentreprise.com/realms/vectis/protocol/openid-connect/token \
  -d "grant_type=client_credentials" \
  -d "client_id=vectis-client" \
  -d "client_secret=secret" | jq -r '.access_token')

# Utiliser le token
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:9081/api/transfers
```

## Bonnes pratiques

### Stockage des credentials

❌ **Ne pas faire** :
```bash
# Credentials en dur dans le code
curl -u admin:password123 ...
```

✅ **À faire** :
```bash
# Via variables d'environnement
curl -u "$PESIT_USER:$PESIT_PASSWORD" ...

# Via fichier .netrc
curl --netrc http://localhost:9081/api/transfers
```

### Rotation des clés

- Générez de nouvelles clés régulièrement (tous les 90 jours)
- Gardez l'ancienne clé active pendant la transition
- Supprimez l'ancienne clé une fois la migration terminée

### Audit

Toutes les authentifications sont journalisées :

```
2025-01-10 10:30:00 INFO  [AUTH] Method=API_KEY KeyId=ak_123456789 IP=192.168.1.100 Status=SUCCESS
2025-01-10 10:30:01 WARN  [AUTH] Method=BASIC User=admin IP=192.168.1.200 Status=FAILED Reason=INVALID_PASSWORD
```
