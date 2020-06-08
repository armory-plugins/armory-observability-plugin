package io.armory.plugin.metrics;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

@Slf4j
public class ArmoryMetricsPlugin extends Plugin {

    public ArmoryMetricsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("The Armory Metrics Plugin has started");
    }

    @Override
    public void stop() {
        log.info("The Armory Metrics Plugin has stopped");
    }
}
