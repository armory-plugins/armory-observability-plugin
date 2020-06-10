# Armory Observability Plugin

Spinnaker plugin for configuring and customizing observability features such as
- Customizing and tweaking the Micrometer registry.
- Enabling direct transmision of metrics to promethous and skipping the [Spinnaker Monitoring Daemon](https://github.com/spinnaker/spinnaker-monitoring/tree/master/spinnaker-monitoring-daemon)

## Potential Future Additions
- Enable distrubuted tracing, Slueth?
- Customize logging to filter noise or have custom appender for shipping important logs to log aggregator
- Customize Error handling? Can we enable something like [Backstopper](https://github.com/Nike-Inc/backstopper) in a plugin? I have no idea ¯\_(ツ)_/¯.

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

## Plugin Configuration

```yaml
spinnaker:
  extensibility:
    plugins:
      Armory.ObservabilityPlugin:
        enabled: true
        config:
          # Human Readable Customer name for dashboarding.
          # Optional, Default: omitted from default tags
          customerName: armory
          # Human Readable Customer Environment name for dashboarding.
          # Optional, Default: omitted from default tags
          customerEnvName: production
          # Halyard generated UUID for non-managed and non-sass customers
          # Optional, Default: omitted from default tags
          customerEnvId: e0fb0422-aa8e-11ea-bb37-0242ac130002
          
          # By default this plugin adds a set of sane default tags to help with observability best practices, you can disable those here
          # Optional, Default: false
          defaultTagsDisabled: false
          
          # By default this plugin does some sane filtering and transformations on metrics, you can disable those here
          # Optional, Default: false
          meterRegistryFiltersDisabled: false

          prometheus:
            # The step size to use in computing windowed statistics like max.
            # To get the most out of these statistics, align the step interval to be close to your scrape interval.
            # Optional, Default: 30
            stepInSeconds: 30
            # true if meter descriptions should be sent to Prometheus.
            # Turn this off to minimize the amount of data sent on each scrape.
            # Optional, Default: false
            descriptions: false
            # Optional, Default: 8009
            scrapePort: 8009
            # Optional, Default: /prometheus
            path: /prometheus
    repositories:
      armory-observability-plugin-releases:
        url: https://raw.githubusercontent.com/armory-observability-plugin-releases/pluginRepository/master/repositories.json            
```
