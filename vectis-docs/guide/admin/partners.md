# Gestion des partenaires

Les partenaires sont les clients autorisés à se connecter à votre serveur Vectis.

![Partners](/screenshots/admin/partners.png)

## Créer un partenaire

### Via l'interface

1. Sélectionnez votre cluster
2. Allez dans **Partners**
3. Cliquez sur **Add Partner**
4. Renseignez les informations du partenaire
5. Cliquez sur **Create**

![Partner Form](/screenshots/admin/partner-form.png)

### Via API

```bash
curl -X POST http://localhost:9080/api/v1/clusters/{clusterId}/partners \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "partnerId": "CLIENT_XYZ",
    "name": "Client XYZ",
    "password": "MotDePasseSecurise123!",
    "enabled": true
  }'
```

## Paramètres

| Paramètre | Description | Obligatoire |
|-----------|-------------|-------------|
| `partnerId` | Identifiant unique (PI_03) | Oui |
| `name` | Nom affiché | Oui |
| `password` | Mot de passe (PI_05) | Non |
| `enabled` | Partenaire actif | Non (défaut: true) |
| `allowedOperations` | READ, WRITE | Non (défaut: les deux) |

## Bonnes pratiques

### Nommage

Utilisez des identifiants explicites :
- ✅ `BANQUE_BNP_PROD`
- ✅ `CLIENT_ACME_123`
- ❌ `P001`
- ❌ `test`

### Mots de passe

- Minimum 12 caractères
- Mélange majuscules/minuscules/chiffres/symboles
- Différent pour chaque partenaire
- Rotation tous les 90 jours

### Permissions

Appliquez le principe du moindre privilège :
- Un partenaire qui envoie uniquement : `WRITE` seulement
- Un partenaire qui récupère uniquement : `READ` seulement

## Désactiver un partenaire

Plutôt que de supprimer, désactivez temporairement :

```bash
curl -X PUT http://localhost:9080/api/v1/clusters/{clusterId}/partners/CLIENT_XYZ \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"enabled": false}'
```

## Audit

Consultez l'historique des connexions d'un partenaire :

```bash
curl "http://localhost:8080/api/transfers?partnerId=CLIENT_XYZ" \
  -u admin:admin
```
