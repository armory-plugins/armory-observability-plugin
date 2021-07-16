## Using Prometheus Operator & Grafana

Assuming that: 
- You've already installed Prometheus Operator to your cluster & deployed a Prometheus CRD
- You have deployed Grafana to your cluster using the prometheus server as datasource
- You have deployed Spinnaker using operator with the Observability [configuration](https://github.com/armory-plugins/armory-observability-plugin/blob/master/README.md#L45-L60) to expose an actuator endpoint for Prometheus  

Deploy a ServiceMonitor to enable discovery of the Spinnaker services endpoints: 
```
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: spinnaker-all-metrics
  labels:
    app: spin
    # this label is here to match the prometheus operator serviceMonitorSelector attribute
    # prometheus.prometheusSpec.serviceMonitorSelector
    # https://github.com/helm/charts/tree/master/stable/prometheus-operator
    release: prometheus-operator
spec:
  selector:
    matchLabels:
      app: spin
    namespaceSelector:
      any: true
  endpoints:
    # "port" is string only. "targetPort" is integer or string.
    - port: http
      interval: 10s
      path: "/aop-prometheus"
``` 
Depending on your configuration modify the path and port or targetPort (when using a targetPort multiple serviceMonitors 
need to be deployed to scrape the appropriate Spinnaker service port).

Alternative, you can deploy multiple PodMonitors to enable discovery of Spinnaker pods endpoints. Example of Clouddriver 
podMonitor:  
```
apiVersion: monitoring.coreos.com/v1
kind: PodMonitor
metadata:
  name: spinnaker-clouddriver
  labels:
    app: spin
    release: prometheus-operator
spec:
  jobLabel: "app.kubernetes.io/name"
  podTargetLabels:
  - "app.kubernetes.io/name"
  namespaceSelector:
    matchNames:
    - spinnaker
  selector:
    matchLabels:
      "app.kubernetes.io/name": clouddriver
  podMetricsEndpoints:
  - port: "7002"
    path: /aop-prometheus
```

OSS community maintains [spinnaker-mixin](https://github.com/uneeq-oss/spinnaker-mixin) as a collection of Grafana dashboards for the metrics exposed by Armory Observability Plugin. 
