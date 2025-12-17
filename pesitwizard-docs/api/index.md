# API Reference

## Vue d'ensemble

PeSIT Wizard Cloud expose trois APIs REST :

| API | Port | Description |
|-----|------|-------------|
| **Client API** | 9081 | Envoi/réception de fichiers |
| **Admin API** | 9080 | Gestion des clusters |
| **Server API** | 8080 | Configuration du serveur |

## Documentation interactive

Chaque API expose une documentation Swagger/OpenAPI :

- Client : http://localhost:9081/swagger-ui.html
- Admin : http://localhost:9080/swagger-ui.html
- Server : http://localhost:8080/swagger-ui.html

## Spécifications OpenAPI

Les fichiers OAS (OpenAPI Specification) sont disponibles :

- [Client API (OAS 3.0)](/api/openapi-client.yaml)
- [Admin API (OAS 3.0)](/api/openapi-admin.yaml)
- [Server API (OAS 3.0)](/api/openapi-server.yaml)

## Format des réponses

Toutes les APIs retournent du JSON :

```json
{
  "data": { ... },
  "error": null,
  "timestamp": "2025-01-10T10:30:00Z"
}
```

### Codes HTTP

| Code | Description |
|------|-------------|
| 200 | Succès |
| 201 | Créé |
| 400 | Requête invalide |
| 401 | Non authentifié |
| 403 | Non autorisé |
| 404 | Non trouvé |
| 500 | Erreur serveur |

### Erreurs

```json
{
  "error": {
    "code": "PARTNER_NOT_FOUND",
    "message": "Partner 'UNKNOWN' not found",
    "details": null
  },
  "timestamp": "2025-01-10T10:30:00Z"
}
```

## Pagination

Les endpoints de liste supportent la pagination :

```bash
GET /api/transfers?page=0&size=20&sort=startTime,desc
```

Réponse :
```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20
}
```

## Filtrage

Utilisez les query parameters pour filtrer :

```bash
GET /api/transfers?status=COMPLETED&direction=SEND&from=2025-01-01
```

## Rate limiting

Les APIs sont limitées à :
- 100 requêtes/minute par IP (API publique)
- 1000 requêtes/minute par token (API authentifiée)

Headers de réponse :
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1704880260
```
