# Armory observability Plugin

Spinnaker plugin for configuring and customizing observability features such as
- Customizing and tweaking the Micrometer registry.
- Enabling direct transmision of metrics to promethous and skipping the [Spinnaker Monitoring Daemon](https://github.com/spinnaker/spinnaker-monitoring/tree/master/spinnaker-monitoring-daemon)

## Potential Future Additions
- Enable distrubuted tracing, Slueth?
- Customize logging to filter noise or have custom appender for shipping important logs to log aggregator
- Customize Error handling? Can we enable something like [Backstopper](https://github.com/Nike-Inc/backstopper) in a plugin? I have no idea ¯\_(ツ)_/¯.

## Development

```bash
./gradlew releaseBundle
```
