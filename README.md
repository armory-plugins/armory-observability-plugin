# Armory Observability Plugin

![CI](https://github.com/armory-plugins/armory-observability-plugin/workflows/CI/badge.svg?branch=master)
[![][release img]][release]
[![][coveralls img]][coveralls]
[![][license img]][license]

Spinnaker plugin for configuring and customizing observability.

## Version Compatibility
 
| Plugin  | Spinnaker Platform |
|:----------- | :--------- |
| 1.+ |  1.20.+ |


## What is Observability?

In control theory, observability is a measure of how well internal states of a system can be inferred from knowledge of its external outputs. The observability and controllability of a linear system are mathematical duals. 

The concept of observability was introduced by Hungarian-American engineer Rudolf E. Kálmán for linear dynamic systems.

<img src="observability.png" width="40%">

In software metrics, logging and tracing make up the core categories of observability

## What does this plugin do?

- Enables customizing and tweaking the Micrometer registry.
- Exposes an [OpenMetrics](https://openmetrics.io/) endpoint for the Micrometer/Spectator metrics
  - This allows tools such as Prometheus or the New Relic OpenMetrics integration to work without the [Spinnaker Monitoring Daemon](https://github.com/spinnaker/spinnaker-monitoring/tree/master/spinnaker-monitoring-daemon).

> WARNING:  Exposing your metrics endpoint CAN be potentially risky if your services are externally accessible. It's HIGHLY recommended that you understand the impacts and security risks.  For example, this exposes
> and endpoint on gate, which then becomes PUBLIC for scraping gate metric data.  You likely should filter that either on ingress or through another security method!  This plugin CURRENTLY does not 
> support enabling this

## Potential Future Additions
- Enable distributed tracing, Slueth?
- Customize logging to filter noise or have custom appender for shipping important logs to log aggregator
- Customize Error handling? Can we enable something like [Backstopper](https://github.com/Nike-Inc/backstopper) in a plugin? I have no idea ¯\\_(ツ)_/¯.
- Ability to move prometheus from endpoint to a separate service on a different port, allowing it to not be exposed

## Plugin Configuration

### Condensed Prometheus Example
```yaml
spinnaker:
  extensibility:
    plugins:
      Armory.ObservabilityPlugin:
        enabled: true
        config.metrics:
          additionalTags:
            customerName: armory
            customerEnvName: production
          prometheus:
            enabled: true
            meterRegistryConfig.armoryRecommendedFiltersEnabled: true
```
OSS community maintains [spinnaker-mixin](https://github.com/uneeq-oss/spinnaker-mixin) as a collection of Grafana dashboards for the metrics exposed by Armory Observability Plugin. 

### Condensed NR Example
```yaml
spinnaker:
  extensibility:
    plugins:
      Armory.ObservabilityPlugin:
        enabled: true
        config.metrics:
          additionalTags:
            customerName: armory
            customerEnvName: fieldju-local
          newrelic:
            enabled: true
            apiKey: encrypted:secrets-manager!r:us-west-2!s:spinnaker-development-secrets!k:new-relic-insert-key
            meterRegistryConfig.armoryRecommendedFiltersEnabled: true
```
### Condensed Datadog Example
```yaml
spinnaker:
  extensibility:
    plugins:
      Armory.ObservabilityPlugin:
        enabled: true
        config.metrics:
          additionalTags:
            customerName: armory
            customerEnvName: manuel-local
          datadog:
            enabled: true
            apiKey: encrypted:k8s!n:datadog!k:apiKey
            applicationKey: encrypted:k8s!n:datadog!k:applicationKey
            meterRegistryConfig.armoryRecommendedFiltersEnabled: true
```
### All Options (we recommend this goes in spinnaker-local.yaml)
```yaml
spinnaker:
  extensibility:
    plugins:
      Armory.ObservabilityPlugin:
        enabled: true
        config:
          metrics:
              # Key value map of extra static default tags to add when generated the default tags
              # Optional, Default: empty map
              additionalTags:
                  # Human Readable Customer name for dashboarding.
                  # Optional, Default: omitted from default tags
                  customerName: armory
                  # Human Readable Customer Environment name for dashboarding.
                  # Optional, Default: omitted from default tags
                  customerEnvName: production
                  # Halyard generated UUID for non-managed and non-sass customers
                  # Optional, Default: omitted from default tags
                  customerEnvId: e0fb0422-aa8e-11ea-bb37-0242ac130002

              # Creates an actuator endpoint for prometheus with id = 'aop-prometheus'
              # See the bottom of this config block
              #
              # By default with the endpoint enabled metrics will be exposed on the service port at /aop-prometheus
              #
              # See: https://gist.github.com/fieldju/7722f36451a652c399db182765046fd3 
              # for adding annotations needed for prometheus to scrape via Halyard.
              # 
              # If you previously used the monitoring daemon to enable prometeus, please note that any dashboards created for that will not work.
              # The official Prometheus Micrometer registry uses the summary metric type for certain metrics in addition to having a different naming convention.
              #
              # See the following gists for the differences:
              # 
              # Monitoring Daemon Output
              # - https://gist.github.com/fieldju/96b8af671f5b7d39d68646329932745e
              # Output from this plugin
              # - https://gist.github.com/fieldju/3174f732eb4b5ad191cabbb678a3f277
              #
              prometheus:
                # Optional, Default: false
                enabled: false
                # The step size to use in computing windowed statistics like max.
                # To get the most out of these statistics, align the step interval to be close to your scrape interval.
                # Optional, Default: 30
                stepInSeconds: 30
                # true if meter descriptions should be sent to Prometheus.
                # Turn this off to minimize the amount of data sent on each scrape.
                # Optional, Default: false
                descriptions: false
                # Config related to configuring, filtering, and transforming Micrometer meters
                meterRegistryConfig: 
                    # By default this plugin adds a set of sane default tags to help with observability best practices, you can disable those here
                    # Optional, Default: false
                    defaultTagsDisabled: false
                    excludedMetricsPrefix: ["Metric1","Metric2"] #configure Metrics to exclude
                    # Configures an opinionated but sane set of default meter filters: https://micrometer.io/docs/concepts#_meter_filters
                    # For example we filter out controller.invocations to prefer the micrometer generated metric 'http.server.requests'
                    # See the following for more details: 
                    # https://github.com/armory-plugins/armory-observability-plugin/blob/master/common/src/main/java/io/armory/plugin/observability/filters/ArmoryRecommendedFilters.java
                    # https://github.com/armory-plugins/armory-observability-plugin/blob/master/common/src/main/java/io/armory/plugin/observability/filters/Filters.java
                    # See bottom of config for controlling percentiles
                    # Optional, Default: false
                    armoryRecommendedFiltersEnabled: true
        
              newrelic:
                # Optional, Default: false
                enabled: false
                # The new relic api key
                # Required if newrelic is enabled
                apiKey: encrypted:secrets-manager!r:us-west-2!s:my-secrets!k:new-relic-api-key
                # The URI for the New Relic metric API. Only necessary if you need to override the default URI.
                # Optional, Default: https://metric-api.newrelic.com/
                uri: https://metric-api.newrelic.com/
                # Turn on "audit mode" in the underlying New Relic Telemetry SDK. This will log all data sent to
                # the New Relic APIs. Be aware that if there is sensitive information in the data being sent that
                # it will be sent to wherever the Telemetry SDK logs are configured to go.
                enableAuditMode: false
                # How often in seconds you want to send metrics to New Relic
                # Optional, Default: 30
                stepInSeconds: 30 
                # The number of threads to use with the scheduler.
                # Optional, Default: 2
                numThreads: 2 
                # The number of measurements per request to use for the backend. If more
                # measurements are found, then multiple requests will be made.
                # Optional, Default: 10000
                batchSize: 10000
                # Config related to configuring, filtering, and transforming Micrometer meters
                meterRegistryConfig: 
                    # By default this plugin adds a set of sane default tags to help with observability best practices, you can disable those here
                    # Optional, Default: false
                    defaultTagsDisabled: false
                    excludedMetricsPrefix: ["Metric1","Metric2"] #configure Metrics to exclude
                    # Configures an opinionated but sane set of default meter filters: https://micrometer.io/docs/concepts#_meter_filters
                    # For example we filter out controller.invocations to prefer the micrometer generated metric 'http.server.requests'
                    # See the following for more details: 
                    # https://github.com/armory-plugins/armory-observability-plugin/blob/master/common/src/main/java/io/armory/plugin/observability/filters/ArmoryRecommendedFilters.java
                    # https://github.com/armory-plugins/armory-observability-plugin/blob/master/common/src/main/java/io/armory/plugin/observability/filters/Filters.java
                    # See bottom of config for controlling percentiles
                    # Optional, Default: false
                    armoryRecommendedFiltersEnabled: true
                    
              datadog:
                # Optional, Default: false
                enabled: false
                # The datadog api key
                # Required if datadog is enabled
                apiKey: encrypted:k8s!n:datadog!k:apiKey
                # The datadog application key
                # Required if datadog is enabled
                applicationKey: encrypted:k8s!n:datadog!k:applicationKey
                # The URI for the Datadog metric API. Only necessary if you need to override the default URI.
                # Optional, Default: https://api.datadoghq.com
                uri: https://api.datadoghq.com
                # How often in seconds you want to send metrics to Datadog
                # Optional, Default: 30
                stepInSeconds: 30
                # The number of measurements per request to use for the backend. If more
                # measurements are found, then multiple requests will be made.
                # There is no way of limit the payload size so a combination of stepInSeconds and 
                # batchSize will avoid the payload too large error
                # Optional, Default: 10000
                batchSize: 10000
                # Config related to configuring, filtering, and transforming Micrometer meters
                meterRegistryConfig:
                  # By default this plugin adds a set of sane default tags to help with observability best practices, you can disable those here
                  # Optional, Default: false
                  defaultTagsDisabled: false
                  excludedMetricsPrefix: ["Metric1","Metric2"] #configure Metrics to exclude
                  # Configures an opinionated but sane set of default meter filters: https://micrometer.io/docs/concepts#_meter_filters
                  # For example we filter out controller.invocations to prefer the micrometer generated metric 'http.server.requests'
                  # See the following for more details: 
                  # https://github.com/armory-plugins/armory-observability-plugin/blob/master/common/src/main/java/io/armory/plugin/observability/filters/ArmoryRecommendedFilters.java
                  # https://github.com/armory-plugins/armory-observability-plugin/blob/master/common/src/main/java/io/armory/plugin/observability/filters/Filters.java
                  # See bottom of config for controlling percentiles
                  # Optional, Default: false
                  armoryRecommendedFiltersEnabled: true
    repositories:
      armory-observability-plugin-releases:
        url: https://raw.githubusercontent.com/armory-plugins/armory-observability-plugin-releases/master/repositories.json
# The prometheus integration utilizes the actuator system therefore it is partially configured under the management settings
# See: https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html for more details 
management:
  # Percentiles for http.server.requests are off by default, if you want them you can add them here
  # When you add these, they will generate extra metrics with percentile = 'xxx' as a tag
  metrics.distribution:
      percentiles[http.server.requests]: 0.95, 0.99
      percentiles-histogram[http.server.requests]: true
  endpoints.web:
    exposure.include: health,info,aop-prometheus
    # You can override the path for any actuator endpoint
    # Optional, Default: aop-prometheus
    path-mapping.prometheus: armory-observability/metrics
  # The port for the actuator endpoints
  # Optional, Default: the server port
  server.port: 9006              
```

## Development

1) Run `./gradlew releaseBundle`
2) Put the `/build/distributions/pf4jPluginWithoutExtensionPoint-X.zip` in the [configured plugins location for your service](https://pf4j.org/doc/packaging.html).
3) [Configure the plugin Spinnaker service.](#plugin-configuration)

To debug the plugin inside a Spinnaker service (like Orca) using IntelliJ Idea follow these steps:

1) Run `./gradlew releaseBundle` in the plugin project.
2) Copy the generated `.plugin-ref` file under `build` in the plugin project submodule for the service to the `plugins` directory under root in the Spinnaker service that will use the plugin .
3) Link the plugin project to the service project in IntelliJ (from the service project use the `+` button in the Gradle tab and select the plugin build.gradle).
4) Configure the Spinnaker service the same way specified above.
5) Create a new IntelliJ run configuration for the service that has the VM option `-Dpf4j.mode=development` and does a `Build Project` before launch.
6) Debug away...

[release img]:https://img.shields.io/github/v/release/armory-plugins/armory-observability-plugin
[release]:https://github.com/armory-plugins/armory-observability-plugin/releases

[coveralls]:https://coveralls.io/github/armory-plugins/armory-observability-plugin
[coveralls img]:https://coveralls.io/repos/github/armory-plugins/armory-observability-plugin/badge.svg?branch=master

[license]:LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg
