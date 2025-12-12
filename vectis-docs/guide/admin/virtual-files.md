# Fichiers virtuels

Les fichiers virtuels définissent les chemins de stockage et les règles de nommage des fichiers échangés.

![Virtual Files](/screenshots/admin/virtual-files.png)

## Concept

Un fichier virtuel est une abstraction qui permet de :
- Définir où stocker les fichiers reçus
- Définir où chercher les fichiers à envoyer
- Appliquer des règles de nommage
- Organiser les échanges par type

## Créer un fichier virtuel

### Via l'interface

1. Sélectionnez votre cluster
2. Allez dans **Virtual Files**
3. Cliquez sur **Add Virtual File**
4. Renseignez les informations du fichier virtuel
5. Cliquez sur **Create**

![Virtual File Form](/screenshots/admin/virtual-file-form.png)

### Via API

```bash
curl -X POST http://localhost:9080/api/v1/clusters/{clusterId}/virtual-files \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "fileId": "VIREMENTS",
    "name": "Fichiers de virements SEPA",
    "sendDirectory": "/data/send/virements",
    "receiveDirectory": "/data/received/virements",
    "filenamePattern": "*.xml"
  }'
```

## Paramètres

| Paramètre | Description |
|-----------|-------------|
| `fileId` | Identifiant unique (PI_12) |
| `name` | Nom affiché |
| `sendDirectory` | Répertoire des fichiers à envoyer |
| `receiveDirectory` | Répertoire des fichiers reçus |
| `receiveFilenamePattern` | Pattern de nommage avec placeholders |
| `overwrite` | Écraser les fichiers existants |
| `direction` | RECEIVE, SEND ou BOTH |

## Pattern de nommage avec placeholders

Pour les fichiers reçus, vous pouvez définir un pattern de nommage dynamique utilisant des placeholders.

### Placeholders disponibles

| Placeholder | Description |
|-------------|-------------|
| `${partner}` | ID du partenaire (PI 3 - Demandeur) |
| `${virtualFile}` | Nom du fichier virtuel (PI 12) |
| `${transferId}` | ID du transfert (PI 13) |
| `${timestamp}` | Horodatage (yyyyMMdd_HHmmss) |
| `${date}` | Date (yyyyMMdd) |
| `${time}` | Heure (HHmmss) |
| `${year}`, `${month}`, `${day}` | Composants de date |
| `${uuid}` | UUID unique |

### Exemples de patterns

```
${partner}/${virtualFile}_${timestamp}
```
Résultat : `PARTNER01/VIREMENTS_20251211_213000`

```
${date}/${partner}_${transferId}
```
Résultat : `20251211/PARTNER01_12345`

::: tip Note Vectis
Le protocole Vectis ne transmet pas le nom du fichier physique source, uniquement l'identifiant du fichier virtuel (PI 12). Les placeholders comme `${file}`, `${basename}`, `${ext}` ne sont donc pas disponibles.
:::

## Organisation recommandée

```
/data
├── send/
│   ├── virements/      # VIREMENTS - fichiers à envoyer
│   ├── prelevements/   # PRELEVEMENTS
│   └── divers/         # DIVERS
│
└── received/
    ├── releves/        # RELEVES - fichiers reçus
    ├── ack/            # ACK - accusés de réception
    └── divers/         # DIVERS
```

## Exemples de configuration

### Virements SEPA

```json
{
  "fileId": "VIREMENTS",
  "name": "Virements SEPA",
  "sendDirectory": "/data/send/virements",
  "receiveDirectory": "/data/received/virements",
  "filenamePattern": "pain.001.*.xml"
}
```

### Relevés de compte

```json
{
  "fileId": "RELEVES",
  "name": "Relevés de compte",
  "sendDirectory": "/data/send/releves",
  "receiveDirectory": "/data/received/releves",
  "filenamePattern": "camt.053.*.xml"
}
```

### Prélèvements SEPA

```json
{
  "fileId": "PRELEVEMENTS",
  "name": "Prélèvements SEPA",
  "sendDirectory": "/data/send/prelevements",
  "receiveDirectory": "/data/received/prelevements",
  "filenamePattern": "pain.008.*.xml"
}
```

## Permissions sur les répertoires

Assurez-vous que le serveur Vectis a les droits d'écriture :

```bash
# Dans le container
chown -R vectis:vectis /data
chmod -R 755 /data
```

## Bonnes pratiques

1. **Un fichier virtuel par type** : Séparez virements, prélèvements, relevés
2. **Nommage explicite** : `VIREMENTS_SEPA` plutôt que `VIR`
3. **Chemins absolus** : Utilisez `/data/...` et non `./data/...`
4. **Patterns restrictifs** : Limitez les extensions acceptées
