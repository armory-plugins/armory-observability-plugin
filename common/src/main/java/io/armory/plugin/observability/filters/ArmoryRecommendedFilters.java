package io.armory.plugin.observability.filters;

import static io.armory.plugin.observability.filters.Filters.*;

import io.micrometer.core.instrument.config.MeterFilter;
import java.util.List;

public class ArmoryRecommendedFilters {

  /*
   * Curated list of filters/transformations that Armory uses internally to reduce DPM / metrics TS cardinality.
   * This list is not guaranteed to have backwards compatibility with Sym versioning of the plugin.
   * What I mean by that is that I will add and remove things to this list that you might rely on without major versioning the plugin.
   *
   * TODO implement pattern that allows for users to pick and choose named filters and not rely on the curated list.
   */
  public static List<MeterFilter> ARMORY_RECOMMENDED_FILTERS =
      List.of(DENY_CONTROLLER_INVOCATIONS_METRICS);
}
