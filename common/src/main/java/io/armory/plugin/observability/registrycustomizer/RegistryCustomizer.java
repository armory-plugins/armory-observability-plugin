package io.armory.plugin.observability.registrycustomizer;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

@Slf4j
public abstract class RegistryCustomizer implements MeterRegistryCustomizer<MeterRegistry> {

    @Override
    public void customize(MeterRegistry registry) {
        var registryClass = registry.getClass().toString();
        try {
            // use reflection to get the actual impl
            // https://github.com/Netflix/spectator/blob/cd320b624d9f28ae67cd285b363afabfcd070006/spectator-reg-micrometer/src/main/java/com/netflix/spectator/micrometer/MicrometerRegistry.java#L46
            // We try catch because 1 day this will break and reflection is generally bad. ¯\(°_o)/¯
            var registryImpl = registry.getClass().getDeclaredField("impl");
            registryClass = registryImpl.getClass().toString();
        } catch (NoSuchFieldException e) {
            // DO NOTHING
        }
        log.info("{} is preparing to customize {} registry class if enabled", this.getClass(), registryClass);
    }

    abstract void doCustomize(MeterRegistry registry);
}
