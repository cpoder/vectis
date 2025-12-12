# Console d'administration

La console d'administration permet de gérer les clusters Vectis déployés sur Kubernetes.

## Accès

URL par défaut : http://localhost:3000

Identifiants par défaut :
- **Utilisateur** : admin
- **Mot de passe** : admin

![Login](/screenshots/admin/login.png)

## Fonctionnalités

### Dashboard

Vue d'ensemble de tous les clusters :
- Statut de santé
- Nombre de pods
- État du déploiement
- Endpoint externe

![Dashboard](/screenshots/admin/dashboard.png)

### Gestion des clusters

![Clusters](/screenshots/admin/clusters.png)

#### Créer un cluster

1. Cliquez sur **Add Cluster**
2. Renseignez les informations du cluster
3. Cliquez sur **Create**

![Cluster Form](/screenshots/admin/cluster-form.png)

#### Détail d'un cluster

Cliquez sur un cluster pour voir ses détails :
- Statut et santé
- Configuration réseau (host, port)
- Liste des pods avec leur statut
- Pod leader (marqué d'une étoile)
- Actions de déploiement

![Cluster Detail](/screenshots/admin/cluster-detail.png)

::: tip Vue complète
La page de détail affiche également la liste des pods en bas de page avec leur adresse IP et leur nœud Kubernetes.
:::

#### Déployer un cluster

1. Sélectionnez le cluster
2. Cliquez sur **Déployer**
3. Attendez que le déploiement soit terminé

Le déploiement crée automatiquement :
- Deployment Kubernetes
- Service LoadBalancer
- PersistentVolumeClaim
- ServiceAccount + RBAC
- ConfigMap

#### Supprimer un cluster

1. Sélectionnez le cluster
2. Cliquez sur **Supprimer**
3. Confirmez la suppression

Toutes les ressources Kubernetes sont supprimées.

### Monitoring

#### Vue des pods

Pour chaque cluster, visualisez :
- Liste des pods
- Statut de chaque pod
- Pod leader (marqué d'une étoile)
- Adresse IP
- Nœud Kubernetes

#### Logs

Consultez les logs de chaque pod directement depuis l'interface.

### Historique des transferts

Consultez l'historique des transferts pour chaque cluster :
- Date et heure
- Direction (SEND/RECEIVE)
- Partenaire
- Fichier virtuel
- Statut (succès/échec)
- Taille et durée

![Transfers](/screenshots/admin/transfers.png)

### Configuration

Depuis la console, configurez :
- Partenaires autorisés
- Fichiers virtuels
- Paramètres du serveur

Voir :
- [Gestion des partenaires](/guide/admin/partners)
- [Fichiers virtuels](/guide/admin/virtual-files)

### Infrastructure

Gérez l'infrastructure de déploiement :
- **Orchestrateurs** : Connexions aux clusters Kubernetes
- **Registres** : Sources d'images Docker

Voir :
- [Orchestrateurs Kubernetes](/guide/admin/orchestrators)
- [Registres Docker](/guide/admin/registries)
