# Registres Docker

Les registres Docker permettent de configurer les sources d'images pour les serveurs Vectis déployés sur Kubernetes.

![Registries](/screenshots/admin/registries.png)

## Ajouter un registre

### Via l'interface

1. Allez dans **Registries**
2. Cliquez sur **Add Registry**
3. Renseignez les informations de connexion
4. Cliquez sur **Create**

![Registry Form](/screenshots/admin/registry-form.png)

## Registres supportés

| Registre | URL | Notes |
|----------|-----|-------|
| Docker Hub | `docker.io` | Registre public par défaut |
| GitHub Container Registry | `ghcr.io` | Utiliser un PAT comme password |
| Google Container Registry | `gcr.io` | Authentification via service account |
| Amazon ECR | `*.dkr.ecr.*.amazonaws.com` | Tokens temporaires |
| Azure Container Registry | `*.azurecr.io` | Service principal |
| Harbor | URL personnalisée | Registre privé open-source |

## Authentification

### Docker Hub

```yaml
name: Docker Hub
url: docker.io
username: myuser
password: dckr_pat_xxxxx
```

### GitHub Container Registry (ghcr.io)

1. Créez un Personal Access Token (PAT) avec les permissions `read:packages`
2. Utilisez votre username GitHub et le PAT comme password

```yaml
name: GitHub Registry
url: ghcr.io
username: my-github-user
password: ghp_xxxxxxxxxxxx
```

### Registre privé

```yaml
name: Private Registry
url: registry.mycompany.com
username: admin
password: secretpassword
```

## Image Pull Secrets

Quand vous créez un registre avec authentification, un `imagePullSecret` est automatiquement créé dans Kubernetes :

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: registry-secret-{id}
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: <base64-encoded-config>
```

Ce secret est référencé automatiquement dans les déploiements Vectis.

## Utilisation dans un cluster

Lors de la création d'un cluster Vectis :

1. Sélectionnez le registre dans **Container Registry**
2. Spécifiez l'image : `ghcr.io/cpoder/vectis-server:latest`
3. Le secret d'authentification est automatiquement associé

## Bonnes pratiques

1. **Utilisez des tokens dédiés** : Créez un token par environnement
2. **Permissions minimales** : `read:packages` suffit pour le pull
3. **Rotation régulière** : Renouvelez les tokens tous les 90 jours
4. **Registre privé pour la prod** : Évitez les registres publics en production
5. **Scan de vulnérabilités** : Activez le scan d'images sur votre registre

## Dépannage

### Erreur "ImagePullBackOff"

1. Vérifiez que le registre est accessible depuis le cluster
2. Vérifiez les credentials
3. Vérifiez que l'image existe

```bash
# Test manuel
kubectl get secret registry-secret-xxx -o jsonpath='{.data.\.dockerconfigjson}' | base64 -d
```

### Token expiré

Mettez à jour le password dans la console et redéployez le cluster.
