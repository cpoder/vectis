# Orchestrateurs Kubernetes

Les orchestrateurs permettent de connecter la console d'administration à vos clusters Kubernetes pour déployer et gérer les serveurs Vectis.

![Orchestrators](/screenshots/admin/orchestrators.png)

## Ajouter un orchestrateur

### Via l'interface

1. Allez dans **Orchestrators**
2. Cliquez sur **Add Orchestrator**
3. Renseignez les informations de connexion
4. Cliquez sur **Create**

![Orchestrator Form](/screenshots/admin/orchestrator-form.png)

### Types d'orchestrateurs supportés

| Type | Description |
|------|-------------|
| `K3S` | K3s (Rancher) - Kubernetes léger |
| `K8S` | Kubernetes standard |
| `EKS` | Amazon Elastic Kubernetes Service |
| `GKE` | Google Kubernetes Engine |
| `AKS` | Azure Kubernetes Service |

## Authentification

### In-Cluster

Pour les déploiements où l'admin tourne dans le même cluster :

```yaml
k8sAuthType: IN_CLUSTER
```

Aucune configuration supplémentaire requise.

### Token Bearer

```yaml
k8sAuthType: TOKEN
k8sToken: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Certificat client

```yaml
k8sAuthType: CERTIFICATE
k8sClientCert: |
  -----BEGIN CERTIFICATE-----
  ...
  -----END CERTIFICATE-----
k8sClientKey: |
  -----BEGIN PRIVATE KEY-----
  ...
  -----END PRIVATE KEY-----
k8sCaCert: |
  -----BEGIN CERTIFICATE-----
  ...
  -----END CERTIFICATE-----
```

## Configuration avancée

### Skip TLS Verify

Pour les environnements de développement avec certificats auto-signés :

```yaml
k8sSkipTlsVerify: true
```

::: warning Attention
Ne jamais utiliser en production !
:::

### MetalLB IP Range

Pour les clusters K3s avec MetalLB :

```yaml
metalLbIpRange: "192.168.1.200-192.168.1.250"
```

## Tester la connexion

Après avoir créé un orchestrateur, testez la connexion :

1. Sélectionnez l'orchestrateur
2. Cliquez sur **Test Connection**
3. Vérifiez que le statut passe à "Connected"

## Bonnes pratiques

1. **Un orchestrateur par environnement** : Séparez dev, staging, prod
2. **Utilisez des ServiceAccounts dédiés** : Créez un SA avec les permissions minimales
3. **Rotation des tokens** : Renouvelez les tokens régulièrement
4. **Audit des accès** : Activez l'audit Kubernetes
