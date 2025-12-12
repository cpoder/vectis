# Server API

Base URL : `http://localhost:8080`

Authentification : Basic Auth (`admin:admin` par défaut)

## Serveurs Vectis

### Liste des serveurs

```http
GET /api/servers
Authorization: Basic YWRtaW46YWRtaW4=
```

**Réponse** :
```json
[
  {
    "serverId": "PESIT_SERVER",
    "port": 5000,
    "status": "RUNNING",
    "autoStart": true,
    "activeConnections": 2
  }
]
```

### Créer un serveur

```http
POST /api/servers
Content-Type: application/json

{
  "serverId": "PESIT_SERVER",
  "port": 5000,
  "autoStart": true,
  "maxConnections": 100,
  "readTimeout": 60000
}
```

### Démarrer un serveur

```http
POST /api/servers/{serverId}/start
```

**Réponse** :
```json
{
  "serverId": "PESIT_SERVER",
  "status": "RUNNING",
  "message": "Server started"
}
```

### Arrêter un serveur

```http
POST /api/servers/{serverId}/stop
```

### Statut d'un serveur

```http
GET /api/servers/{serverId}/status
```

**Réponse** :
```json
{
  "serverId": "PESIT_SERVER",
  "status": "RUNNING",
  "port": 5000,
  "activeConnections": 2,
  "totalTransfers": 1523,
  "uptime": 86400
}
```

## Partenaires

### Liste des partenaires

```http
GET /api/partners
```

**Réponse** :
```json
[
  {
    "partnerId": "CLIENT_XYZ",
    "name": "Client XYZ",
    "enabled": true,
    "lastConnection": "2025-01-10T10:30:00Z",
    "transferCount": 150
  }
]
```

### Créer un partenaire

```http
POST /api/partners
Content-Type: application/json

{
  "partnerId": "CLIENT_XYZ",
  "name": "Client XYZ",
  "password": "secret123",
  "enabled": true,
  "allowedOperations": ["READ", "WRITE"]
}
```

### Modifier un partenaire

```http
PUT /api/partners/{partnerId}
Content-Type: application/json

{
  "name": "Client XYZ (Production)",
  "enabled": true
}
```

### Supprimer un partenaire

```http
DELETE /api/partners/{partnerId}
```

### Changer le mot de passe

```http
POST /api/partners/{partnerId}/password
Content-Type: application/json

{
  "password": "newpassword123"
}
```

## Fichiers virtuels

### Liste des fichiers virtuels

```http
GET /api/virtual-files
```

**Réponse** :
```json
[
  {
    "fileId": "VIREMENTS",
    "name": "Fichiers de virements",
    "sendDirectory": "/data/send/virements",
    "receiveDirectory": "/data/received/virements",
    "filenamePattern": "*.xml"
  }
]
```

### Créer un fichier virtuel

```http
POST /api/virtual-files
Content-Type: application/json

{
  "fileId": "VIREMENTS",
  "name": "Fichiers de virements",
  "sendDirectory": "/data/send/virements",
  "receiveDirectory": "/data/received/virements",
  "filenamePattern": "*.xml"
}
```

### Modifier un fichier virtuel

```http
PUT /api/virtual-files/{fileId}
```

### Supprimer un fichier virtuel

```http
DELETE /api/virtual-files/{fileId}
```

## Cluster

### Statut du cluster

```http
GET /api/cluster/status
```

**Réponse** :
```json
{
  "clusterName": "vectis-cluster",
  "isLeader": true,
  "nodeName": "vectis-server-abc123",
  "members": [
    {
      "name": "vectis-server-abc123",
      "address": "10.42.0.100",
      "isLeader": true
    },
    {
      "name": "vectis-server-def456",
      "address": "10.42.0.101",
      "isLeader": false
    }
  ],
  "memberCount": 2
}
```

## Historique des transferts

### Liste des transferts

```http
GET /api/transfers?page=0&size=20
```

**Query parameters** :

| Paramètre | Description |
|-----------|-------------|
| `partnerId` | Filtrer par partenaire |
| `direction` | SEND ou RECEIVE |
| `status` | COMPLETED, FAILED |
| `from` | Date de début |
| `to` | Date de fin |

**Réponse** :
```json
{
  "content": [
    {
      "id": 1,
      "partnerId": "CLIENT_XYZ",
      "direction": "RECEIVE",
      "filename": "VIREMENT.XML",
      "virtualFileId": "VIREMENTS",
      "size": 15234,
      "status": "COMPLETED",
      "startTime": "2025-01-10T10:30:00Z",
      "endTime": "2025-01-10T10:30:05Z"
    }
  ],
  "totalElements": 1523
}
```

## Health & Monitoring

### Health check

```http
GET /actuator/health
```

### Métriques Prometheus

```http
GET /actuator/prometheus
```

### Info

```http
GET /actuator/info
```
