# Client API

Base URL : `http://localhost:9081`

## Serveurs

### Liste des serveurs

```http
GET /api/servers
```

**Réponse** :
```json
[
  {
    "id": 1,
    "name": "BNP Paribas",
    "host": "vectis.bnpparibas.com",
    "port": 5000,
    "serverId": "BNPP_SERVER",
    "clientId": "MON_ENTREPRISE",
    "tlsEnabled": true,
    "status": "CONNECTED"
  }
]
```

### Créer un serveur

```http
POST /api/servers
Content-Type: application/json

{
  "name": "BNP Paribas",
  "host": "vectis.bnpparibas.com",
  "port": 5000,
  "serverId": "BNPP_SERVER",
  "clientId": "MON_ENTREPRISE",
  "password": "secret123",
  "tlsEnabled": true
}
```

### Modifier un serveur

```http
PUT /api/servers/{id}
Content-Type: application/json

{
  "name": "BNP Paribas Production",
  "password": "newpassword"
}
```

### Supprimer un serveur

```http
DELETE /api/servers/{id}
```

### Tester la connexion

```http
POST /api/servers/{id}/test
```

**Réponse** :
```json
{
  "success": true,
  "latency": 45,
  "serverVersion": 2,
  "message": "Connection successful"
}
```

## Transferts

### Envoyer un fichier

```http
POST /api/transfers/send
Content-Type: multipart/form-data

file: (binary)
serverId: 1
remoteFilename: VIREMENT_20250110.XML
partnerId: MON_ENTREPRISE
virtualFile: VIREMENTS
```

**Réponse** :
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "direction": "SEND",
  "filename": "VIREMENT_20250110.XML",
  "size": 15234,
  "startTime": "2025-01-10T10:30:00Z",
  "endTime": "2025-01-10T10:30:05Z",
  "duration": 5000
}
```

### Recevoir un fichier

```http
POST /api/transfers/receive
Content-Type: application/json

{
  "serverId": 1,
  "remoteFilename": "RELEVE_20250110.XML",
  "partnerId": "MON_ENTREPRISE",
  "virtualFile": "RELEVES"
}
```

**Réponse** :
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "status": "COMPLETED",
  "direction": "RECEIVE",
  "filename": "RELEVE_20250110.XML",
  "localPath": "/data/received/RELEVE_20250110.XML",
  "size": 8542
}
```

### Télécharger un fichier reçu

```http
GET /api/transfers/{id}/download
```

Retourne le fichier binaire avec les headers appropriés.

### Liste des transferts

```http
GET /api/transfers?page=0&size=20
```

**Query parameters** :

| Paramètre | Type | Description |
|-----------|------|-------------|
| `page` | int | Numéro de page (0-indexed) |
| `size` | int | Taille de page (défaut: 20) |
| `sort` | string | Tri (ex: `startTime,desc`) |
| `status` | string | Filtrer par statut |
| `direction` | string | SEND ou RECEIVE |
| `serverId` | int | Filtrer par serveur |
| `from` | date | Date de début |
| `to` | date | Date de fin |

**Réponse** :
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "status": "COMPLETED",
      "direction": "SEND",
      "filename": "VIREMENT_20250110.XML",
      "serverName": "BNP Paribas",
      "size": 15234,
      "startTime": "2025-01-10T10:30:00Z"
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20
}
```

### Détail d'un transfert

```http
GET /api/transfers/{id}
```

### Annuler un transfert

```http
POST /api/transfers/{id}/cancel
```

## Statuts de transfert

| Statut | Description |
|--------|-------------|
| `PENDING` | En attente |
| `IN_PROGRESS` | En cours |
| `COMPLETED` | Terminé avec succès |
| `FAILED` | Échec |
| `CANCELLED` | Annulé |

## Codes d'erreur

| Code | Description |
|------|-------------|
| `SERVER_NOT_FOUND` | Serveur non trouvé |
| `CONNECTION_FAILED` | Connexion impossible |
| `AUTH_FAILED` | Authentification échouée |
| `PARTNER_UNKNOWN` | Partenaire non reconnu |
| `FILE_NOT_FOUND` | Fichier non trouvé |
| `TRANSFER_FAILED` | Échec du transfert |
| `TIMEOUT` | Délai dépassé |
