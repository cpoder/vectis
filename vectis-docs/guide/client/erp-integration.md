# Intégration ERP

## Vue d'ensemble

Vectis Cloud expose une API REST qui permet d'intégrer facilement les transferts de fichiers dans vos processus métier.

## Cas d'usage typiques

### 1. Export des virements depuis l'ERP

```
┌─────────┐     ┌─────────────┐     ┌─────────────┐     ┌────────┐
│   ERP   │────▶│ Fichier XML │────▶│ Vectis Client│────▶│ Banque │
└─────────┘     └─────────────┘     └─────────────┘     └────────┘
```

Votre ERP génère un fichier de virements SEPA, puis appelle l'API Vectis pour l'envoyer.

### 2. Import des relevés dans la comptabilité

```
┌────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────┐
│ Banque │────▶│ Vectis Client│────▶│ Fichier XML │────▶│ Compta  │
└────────┘     └─────────────┘     └─────────────┘     └─────────┘
```

Vectis Client récupère les relevés, puis votre logiciel comptable les importe.

## Intégration par webhook

### Configuration

Configurez un webhook pour être notifié des transferts :

```bash
curl -X POST http://localhost:9081/api/webhooks \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://mon-erp.com/api/vectis/callback",
    "events": ["TRANSFER_COMPLETED", "TRANSFER_FAILED"],
    "secret": "mon-secret-webhook"
  }'
```

### Payload reçu

```json
{
  "event": "TRANSFER_COMPLETED",
  "timestamp": "2025-01-10T10:30:05Z",
  "transfer": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "direction": "RECEIVE",
    "filename": "RELEVE_20250110.XML",
    "localPath": "/data/received/RELEVE_20250110.XML",
    "size": 8542
  }
}
```

### Vérification de signature

```python
import hmac
import hashlib

def verify_webhook(payload, signature, secret):
    expected = hmac.new(
        secret.encode(),
        payload.encode(),
        hashlib.sha256
    ).hexdigest()
    return hmac.compare_digest(f"sha256={expected}", signature)
```

## Exemples d'intégration

### Python

```python
import requests

class PesitClient:
    def __init__(self, base_url, api_key=None):
        self.base_url = base_url
        self.session = requests.Session()
        if api_key:
            self.session.headers['Authorization'] = f'Bearer {api_key}'
    
    def send_file(self, server_id, file_path, remote_name, partner_id, virtual_file):
        with open(file_path, 'rb') as f:
            response = self.session.post(
                f'{self.base_url}/api/transfers/send',
                files={'file': f},
                data={
                    'serverId': server_id,
                    'remoteFilename': remote_name,
                    'partnerId': partner_id,
                    'virtualFile': virtual_file
                }
            )
        return response.json()
    
    def receive_file(self, server_id, remote_name, partner_id, virtual_file):
        response = self.session.post(
            f'{self.base_url}/api/transfers/receive',
            json={
                'serverId': server_id,
                'remoteFilename': remote_name,
                'partnerId': partner_id,
                'virtualFile': virtual_file
            }
        )
        return response.json()
    
    def download_file(self, transfer_id, local_path):
        response = self.session.get(
            f'{self.base_url}/api/transfers/{transfer_id}/download',
            stream=True
        )
        with open(local_path, 'wb') as f:
            for chunk in response.iter_content(chunk_size=8192):
                f.write(chunk)

# Utilisation
client = PesitClient('http://localhost:9081')

# Envoyer un virement
result = client.send_file(
    server_id=1,
    file_path='/data/virements/virement_20250110.xml',
    remote_name='VIREMENT_20250110.XML',
    partner_id='MON_ENTREPRISE',
    virtual_file='VIREMENTS'
)
print(f"Transfert {result['status']}: {result['id']}")

# Recevoir un relevé
result = client.receive_file(
    server_id=1,
    remote_name='RELEVE_20250110.XML',
    partner_id='MON_ENTREPRISE',
    virtual_file='RELEVES'
)
if result['status'] == 'COMPLETED':
    client.download_file(result['id'], '/data/releves/releve_20250110.xml')
```

### Node.js

```javascript
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');

class PesitClient {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
  }

  async sendFile(serverId, filePath, remoteName, partnerId, virtualFile) {
    const form = new FormData();
    form.append('file', fs.createReadStream(filePath));
    form.append('serverId', serverId);
    form.append('remoteFilename', remoteName);
    form.append('partnerId', partnerId);
    form.append('virtualFile', virtualFile);

    const response = await axios.post(
      `${this.baseUrl}/api/transfers/send`,
      form,
      { headers: form.getHeaders() }
    );
    return response.data;
  }

  async receiveFile(serverId, remoteName, partnerId, virtualFile) {
    const response = await axios.post(
      `${this.baseUrl}/api/transfers/receive`,
      { serverId, remoteFilename: remoteName, partnerId, virtualFile }
    );
    return response.data;
  }
}

// Utilisation
const client = new PesitClient('http://localhost:9081');

async function main() {
  // Envoyer
  const sendResult = await client.sendFile(
    1, './virement.xml', 'VIREMENT.XML', 'MON_ENTREPRISE', 'VIREMENTS'
  );
  console.log('Envoi:', sendResult.status);

  // Recevoir
  const receiveResult = await client.receiveFile(
    1, 'RELEVE.XML', 'MON_ENTREPRISE', 'RELEVES'
  );
  console.log('Réception:', receiveResult.status);
}

main();
```

### Java

```java
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.http.*;

public class PesitClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PesitClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    public TransferResult sendFile(int serverId, String filePath, 
            String remoteName, String partnerId, String virtualFile) {
        
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath));
        body.add("serverId", serverId);
        body.add("remoteFilename", remoteName);
        body.add("partnerId", partnerId);
        body.add("virtualFile", virtualFile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<TransferResult> response = restTemplate.exchange(
            baseUrl + "/api/transfers/send",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            TransferResult.class
        );
        return response.getBody();
    }
}
```

## Formats de fichiers supportés

| Format | Description | Usage |
|--------|-------------|-------|
| CFONB 160 | Virements domestiques | Legacy |
| CFONB 240 | Relevés de compte | Legacy |
| pain.001 | Virements SEPA (XML) | Standard |
| pain.008 | Prélèvements SEPA (XML) | Standard |
| camt.053 | Relevés de compte (XML) | Standard |

Vectis Cloud transporte les fichiers de manière transparente - le format est géré par votre ERP et votre banque.
