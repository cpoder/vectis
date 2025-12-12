# Monitoring

## Métriques disponibles

Le serveur Vectis expose des métriques Prometheus sur `/actuator/prometheus`.

### Métriques clés

| Métrique | Description |
|----------|-------------|
| `vectis_connections_active` | Connexions Vectis actives |
| `vectis_connections_total` | Total des connexions |
| `vectis_transfers_total` | Nombre de transferts |
| `vectis_transfers_bytes_total` | Volume transféré (bytes) |
| `vectis_transfers_duration_seconds` | Durée des transferts |
| `vectis_errors_total` | Nombre d'erreurs |
| `vectis_cluster_members` | Membres du cluster |
| `vectis_cluster_is_leader` | 1 si leader, 0 sinon |

## Intégration Prometheus

### Configuration Prometheus

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'vectis-server'
    kubernetes_sd_configs:
      - role: pod
        namespaces:
          names: ['vectis']
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_label_app]
        regex: vectis-server
        action: keep
      - source_labels: [__meta_kubernetes_pod_container_port_number]
        regex: "8080"
        action: keep
```

### Requêtes utiles

```promql
# Taux de transferts par minute
rate(vectis_transfers_total[5m]) * 60

# Volume transféré par heure
increase(vectis_transfers_bytes_total[1h])

# Taux d'erreur
rate(vectis_errors_total[5m]) / rate(vectis_transfers_total[5m])

# Durée moyenne des transferts
rate(vectis_transfers_duration_seconds_sum[5m]) / rate(vectis_transfers_duration_seconds_count[5m])
```

## Dashboards Grafana

### Dashboard principal

Importez le dashboard depuis : `/grafana/vectis-dashboard.json`

Panels inclus :
- Transferts par minute
- Volume transféré
- Connexions actives
- Taux d'erreur
- Statut du cluster
- Top partenaires

### Alertes recommandées

```yaml
# alerting-rules.yml
groups:
  - name: vectis
    rules:
      - alert: PesitHighErrorRate
        expr: rate(vectis_errors_total[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Taux d'erreur Vectis élevé"
          
      - alert: PesitNoLeader
        expr: sum(vectis_cluster_is_leader) == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Pas de leader Vectis"
          
      - alert: PesitClusterDegraded
        expr: vectis_cluster_members < 3
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Cluster Vectis dégradé"
```

## Logs

### Format des logs

```
2025-01-10 10:30:00.123 INFO  [vectis-server] [session-123] CONNECT partner=CLIENT_XYZ ip=192.168.1.100
2025-01-10 10:30:01.456 INFO  [vectis-server] [session-123] CREATE file=VIREMENT.XML virtualFile=VIREMENTS
2025-01-10 10:30:05.789 INFO  [vectis-server] [session-123] TRANSFER_COMPLETE bytes=15234 duration=4333ms
```

### Centralisation avec ELK

```yaml
# filebeat.yml
filebeat.inputs:
  - type: container
    paths:
      - /var/log/containers/vectis-server-*.log
    processors:
      - add_kubernetes_metadata: ~

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "vectis-%{+yyyy.MM.dd}"
```

### Requêtes Kibana utiles

```
# Erreurs des dernières 24h
level:ERROR AND kubernetes.labels.app:vectis-server

# Transferts d'un partenaire
message:"TRANSFER_COMPLETE" AND partner:CLIENT_XYZ

# Connexions échouées
message:"CONNECT" AND status:FAILED
```

## Health checks

### Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Santé globale |
| `/actuator/health/readiness` | Prêt à recevoir du trafic |
| `/actuator/health/liveness` | Application en vie |

### Kubernetes probes

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30
```

## Alerting

### Email

Configurez les alertes email dans l'application :

```yaml
vectis:
  alerting:
    email:
      enabled: true
      smtp-host: smtp.example.com
      from: vectis@example.com
      to: ops@example.com
    triggers:
      - type: TRANSFER_FAILED
      - type: CONNECTION_FAILED
      - type: CLUSTER_DEGRADED
```

### Webhook

```yaml
vectis:
  alerting:
    webhook:
      enabled: true
      url: https://hooks.slack.com/services/xxx
      events:
        - TRANSFER_FAILED
        - CLUSTER_LEADER_CHANGED
```
