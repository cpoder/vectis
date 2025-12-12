# Admin API

Base URL : `http://localhost:9080`

Authentification : Basic Auth (`admin:admin` par défaut)

## Clusters

### Liste des clusters

```http
GET /api/v1/clusters
Authorization: Basic YWRtaW46YWRtaW4=
```

**Réponse** :
```json
[
  {
    "id": "105a1205-fecf-425f-9eff-9dc0beb1fd24",
    "name": "Production Vectis",
    "namespace": "vectis-prod",
    "environment": "PRODUCTION",
    "status": "HEALTHY",
    "deploymentState": "DEPLOYED",
    "replicas": 3,
    "readyReplicas": 3,
    "externalEndpoint": "10.0.0.100:5000"
  }
]
```

### Créer un cluster

```http
POST /api/v1/clusters
Content-Type: application/json
Authorization: Basic YWRtaW46YWRtaW4=

{
  "name": "Production Vectis",
  "namespace": "vectis-prod",
  "environment": "PRODUCTION",
  "replicas": 3,
  "vectisPort": 5000,
  "containerImage": "ghcr.io/cpoder/vectis-server:latest",
  "storageHostPath": "/data/vectis"
}
```

**Paramètres** :

| Champ | Type | Obligatoire | Description |
|-------|------|-------------|-------------|
| `name` | string | Oui | Nom du cluster |
| `namespace` | string | Oui | Namespace Kubernetes |
| `environment` | enum | Non | DEVELOPMENT, STAGING, PRODUCTION |
| `replicas` | int | Non | Nombre de replicas (défaut: 3) |
| `vectisPort` | int | Non | Port Vectis (défaut: 5000) |
| `containerImage` | string | Oui | Image Docker |
| `storageHostPath` | string | Non | Chemin de stockage sur l'hôte |

### Détail d'un cluster

```http
GET /api/v1/clusters/{id}
```

### Modifier un cluster

```http
PUT /api/v1/clusters/{id}
Content-Type: application/json

{
  "replicas": 5,
  "environment": "PRODUCTION"
}
```

### Supprimer un cluster

```http
DELETE /api/v1/clusters/{id}
```

Supprime le cluster et toutes les ressources Kubernetes associées.

## Déploiement

### Déployer un cluster

```http
POST /api/v1/clusters/{id}/deploy
```

Crée les ressources Kubernetes :
- Deployment
- Service (LoadBalancer)
- PersistentVolumeClaim
- ServiceAccount + RBAC
- ConfigMap

### Redéployer un cluster

```http
POST /api/v1/clusters/{id}/deploy?force=true
```

### Statut du déploiement

```http
GET /api/v1/clusters/{id}/deployment/status
```

**Réponse** :
```json
{
  "deploymentName": "vectis-server",
  "namespace": "vectis-prod",
  "status": "healthy",
  "replicas": 3,
  "readyReplicas": 3,
  "availableReplicas": 3,
  "runningPods": 3,
  "error": null
}
```

## Pods

### Liste des pods

```http
GET /api/v1/clusters/{id}/pods
```

**Réponse** :
```json
[
  {
    "name": "vectis-server-abc123",
    "status": "Running",
    "ready": true,
    "isLeader": true,
    "ip": "10.42.0.100",
    "node": "worker-1",
    "startTime": "2025-01-10T08:00:00Z"
  },
  {
    "name": "vectis-server-def456",
    "status": "Running",
    "ready": true,
    "isLeader": false,
    "ip": "10.42.0.101",
    "node": "worker-2"
  }
]
```

### Logs d'un pod

```http
GET /api/v1/clusters/{id}/pods/{podName}/logs?lines=100
```

## Configuration du serveur

### Partenaires

```http
GET /api/v1/clusters/{id}/partners
POST /api/v1/clusters/{id}/partners
PUT /api/v1/clusters/{id}/partners/{partnerId}
DELETE /api/v1/clusters/{id}/partners/{partnerId}
```

**Créer un partenaire** :
```json
{
  "partnerId": "CLIENT_XYZ",
  "name": "Client XYZ",
  "password": "secret123",
  "enabled": true
}
```

### Fichiers virtuels

```http
GET /api/v1/clusters/{id}/virtual-files
POST /api/v1/clusters/{id}/virtual-files
PUT /api/v1/clusters/{id}/virtual-files/{fileId}
DELETE /api/v1/clusters/{id}/virtual-files/{fileId}
```

**Créer un fichier virtuel** :
```json
{
  "fileId": "VIREMENTS",
  "name": "Fichiers de virements",
  "sendDirectory": "/data/send/virements",
  "receiveDirectory": "/data/received/virements"
}
```

## États du cluster

| État | Description |
|------|-------------|
| `NOT_DEPLOYED` | Pas encore déployé |
| `DEPLOYING` | Déploiement en cours |
| `DEPLOYED` | Déployé avec succès |
| `FAILED` | Échec du déploiement |
| `DELETING` | Suppression en cours |

## Statuts de santé

| Statut | Description |
|--------|-------------|
| `HEALTHY` | Tous les pods sont prêts |
| `DEGRADED` | Certains pods ne sont pas prêts |
| `UNHEALTHY` | Aucun pod n'est prêt |
