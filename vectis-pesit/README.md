# PeSIT Java Library

Bibliothèque Java implémentant le protocole PeSIT (Protocole d'Échange pour un Système Interbancaire de Télécompensation). Utilisée par `pesit-server` et `pesit-client`.

## Fonctionnalités

- **Encodage/décodage FPDU** : Sérialisation binaire conforme à la spécification PeSIT E
- **Tous les types de messages** : CONNECT, CREATE, SELECT, OPEN, WRITE, READ, DTF, etc.
- **Paramètres PeSIT** : Support complet des PI (Parameter Identifier) et PGI (Parameter Group Identifier)
- **Session PeSIT** : Gestion des connexions TCP et échanges de messages

## Installation

```xml
<dependency>
    <groupId>com.pesit</groupId>
    <artifactId>pesit-protocol</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Build

```bash
mvn clean install
```

## Utilisation

### Créer une connexion

```java
import com.pesit.protocol.Fpdu;
import com.pesit.protocol.FpduType;
import com.pesit.protocol.ParameterValue;
import static com.pesit.protocol.ParameterIdentifier.*;

// Créer un FPDU CONNECT
Fpdu connect = new Fpdu(FpduType.CONNECT)
    .withIdSrc(1)
    .withParameter(new ParameterValue(PI_03_DEMANDEUR, "MY_CLIENT"))
    .withParameter(new ParameterValue(PI_04_SERVEUR, "BANK_SERVER"))
    .withParameter(new ParameterValue(PI_06_VERSION, 2));

// Sérialiser en bytes
byte[] data = connect.toBytes();
```

### Décoder un FPDU

```java
byte[] received = // ... données reçues du réseau
Fpdu fpdu = Fpdu.fromBytes(received);

if (fpdu.getType() == FpduType.ACONNECT) {
    int serverId = fpdu.getIdSrc();
    // ...
}
```

## Prérequis

- Java 21+
- Maven 3.6+

## Référence

- Spécification PeSIT Version E (Septembre 1989) - GSIT
