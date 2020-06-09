package io.armory.plugin.observability;

import io.micrometer.core.instrument.config.MeterFilter;

import java.util.List;

import static io.micrometer.core.instrument.config.MeterFilter.deny;

public class MeterFilters {

    /**
     * This may be too primitive but, it can be a place to start.
     *
     * Denies metrics that contain the word "percentile" in their name.
     */
    private static final MeterFilter BLOCK_METRICS_THAT_HAVE_PERCENTILE_IN_NAME =
            deny(id -> id.getName().toLowerCase().contains("percentile"));

    public static List<MeterFilter> METER_FILTERS = List.of(
            BLOCK_METRICS_THAT_HAVE_PERCENTILE_IN_NAME
    );
}
