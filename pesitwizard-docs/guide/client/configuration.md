# Configuration du Client

## Variables d'environnement

| Variable | Description | Défaut |
|----------|-------------|--------|
| `SERVER_PORT` | Port HTTP de l'API | 9081 |
| `SPRING_DATASOURCE_URL` | URL JDBC PostgreSQL | jdbc:h2:mem:pesitwizard |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur DB | pesitwizard |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe DB | pesitwizard |

## Fichier application.yml

```yaml
server:
  port: 9081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pesitwizard
    username: pesitwizard
    password: pesitwizard
  jpa:
    hibernate:
      ddl-auto: update

# Configuration des transferts
pesitwizard:
  client:
    # Répertoire de stockage des fichiers reçus
    receive-directory: /data/received
    
    # Répertoire temporaire pour les uploads
    temp-directory: /data/temp
    
    # Timeout de connexion (ms)
    connection-timeout: 30000
    
    # Timeout de lecture (ms)
    read-timeout: 60000
    
    # Nombre de tentatives en cas d'échec
    retry-count: 3
    
    # Délai entre les tentatives (ms)
    retry-delay: 5000

# Logging
logging:
  level:
    com.pesitwizard: INFO
    # Pour debug PeSIT Wizard
    # com.pesitwizard.protocol: DEBUG
```

## Configuration des serveurs cibles

Les serveurs PeSIT cibles sont configurés via l'API ou l'interface web.

### Via API

```bash
curl -X POST http://localhost:9081/api/servers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "BNP Paribas",
    "host": "pesitwizard.bnpparibas.com",
    "port": 5000,
    "serverId": "BNPP_SERVER",
    "clientId": "MON_ENTREPRISE",
    "password": "secret123",
    "tlsEnabled": true
  }'
```

### Paramètres serveur

| Paramètre | Description | Obligatoire |
|-----------|-------------|-------------|
| `name` | Nom affiché | Oui |
| `host` | Adresse du serveur | Oui |
| `port` | Port PeSIT | Oui (défaut: 5000) |
| `serverId` | Identifiant serveur (PI_04) | Oui |
| `clientId` | Votre identifiant (PI_03) | Oui |
| `password` | Mot de passe (PI_05) | Non |
| `tlsEnabled` | Activer TLS | Non (défaut: false) |

## Configuration TLS

Pour les connexions sécurisées (PeSIT-E) :

### Générer un keystore client

```bash
# Générer une paire de clés
keytool -genkeypair \
  -alias client \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -keystore client-keystore.p12 \
  -storetype PKCS12 \
  -storepass changeit \
  -dname "CN=Mon Entreprise, O=Mon Entreprise, C=FR"

# Exporter le certificat (à envoyer à la banque)
keytool -exportcert \
  -alias client \
  -keystore client-keystore.p12 \
  -storetype PKCS12 \
  -storepass changeit \
  -file client.crt
```

### Importer le certificat de la banque

```bash
# Importer le certificat serveur dans le truststore
keytool -importcert \
  -alias banque \
  -file banque-server.crt \
  -keystore client-truststore.p12 \
  -storetype PKCS12 \
  -storepass changeit \
  -noprompt
```

### Configuration application.yml

```yaml
pesitwizard:
  client:
    tls:
      enabled: true
      keystore-path: /config/client-keystore.p12
      keystore-password: changeit
      truststore-path: /config/client-truststore.p12
      truststore-password: changeit
```

## Proxy HTTP

Si vous êtes derrière un proxy :

```yaml
pesitwizard:
  client:
    proxy:
      enabled: true
      host: proxy.entreprise.com
      port: 8080
      username: user  # optionnel
      password: pass  # optionnel
```

## Logs et debug

Pour activer les logs détaillés du protocole PeSIT :

```yaml
logging:
  level:
    com.pesitwizard.protocol: DEBUG
    com.pesitwizard.client.service: DEBUG
```

Les logs afficheront :
- Connexions/déconnexions
- Messages FPDU échangés
- Données transférées (en hexadécimal)
