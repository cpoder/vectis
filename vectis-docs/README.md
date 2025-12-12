# Vectis Documentation

Site de documentation pour la solution Vectis Cloud, construit avec VitePress.

## Développement

```bash
# Installer les dépendances
npm install

# Lancer le serveur de développement
npm run dev
```

Le site sera accessible sur http://localhost:5173

## Build

```bash
npm run build
```

Les fichiers statiques seront générés dans `.vitepress/dist/`.

## Structure

```
vectis-docs/
├── .vitepress/
│   └── config.ts          # Configuration VitePress
├── public/
│   └── api/               # Fichiers OpenAPI (OAS)
│       ├── openapi-client.yaml
│       ├── openapi-admin.yaml
│       └── openapi-server.yaml
├── guide/
│   ├── index.md           # Introduction
│   ├── quickstart.md      # Démarrage rapide
│   ├── architecture.md    # Architecture
│   ├── client/            # Documentation client
│   ├── server/            # Documentation serveur
│   └── admin/             # Documentation admin
├── api/
│   ├── index.md           # Vue d'ensemble API
│   ├── authentication.md  # Authentification
│   ├── client.md          # Client API
│   ├── admin.md           # Admin API
│   └── server.md          # Server API
├── pricing.md             # Page tarifs
└── index.md               # Page d'accueil
```

## Déploiement

### Netlify

```bash
npm run build
# Déployer .vitepress/dist/
```

### GitHub Pages

Le site peut être déployé automatiquement via GitHub Actions.

## Fichiers OpenAPI

Les spécifications OpenAPI sont disponibles dans `public/api/` :

- `openapi-client.yaml` - API Client (port 9081)
- `openapi-admin.yaml` - API Admin (port 9080)
- `openapi-server.yaml` - API Server (port 8080)

Ces fichiers peuvent être importés dans Postman, Insomnia, ou utilisés pour générer des clients SDK.
