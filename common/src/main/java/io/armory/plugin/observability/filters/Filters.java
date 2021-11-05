package io.armory.plugin.observability.filters;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;

public class Filters {

  /*
   * Removes the spinnaker created 'controller.invocations' metric to prefer the micrometer created 'http.server.requests' metric
   * They both contain http metrics, however http.server.requests is non-java / sb specific and allows for dashboards that can interoperate x-framework
   *
   * https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-metrics-spring-mvc
   *
   * Extra details are off by default and opt in
   * The preferred way to add percentiles, percentile histograms, and SLA boundaries is to apply the general
   * purpose property-based meter filter mechanism to this timer:
   *
   * management.metrics.distribution:
   *  percentiles[http.server.requests]: 0.95, 0.99
   *  percentiles-histogram[http.server.requests]: true
   *  sla[http.server.requests]: 10ms, 100ms
   *
   * See: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#per-meter-properties
   */
  public static final MeterFilter DENY_CONTROLLER_INVOCATIONS_METRICS = MeterFilter.denyNameStartsWith("controller.invocations");
  public static final MeterFilter WHITELIST_ONLY_FILTER = MeterFilter.denyUnless(Filters::isInWhiteListOnly);

  private static boolean isInWhiteListOnly(Meter.Id id) {

    //This is for metrics that the observability plugin has access to - some of these may not be relevant, aka NR metrics, for many cases... but just in case :)
    return id.getName().startsWith("jdbc.connections") //jdbc metrics
            || id.getName().startsWith("http.server") //http request metrics
            || id.getName().startsWith("controller") // HTTP Controller type metrics.  This is ALMOST redundant with http metrics, but the controller metrics have some more tags that are useful so we allow it in some cases
            || id.getName().startsWith("spinnakerRelease") //Info type metrics (aka for version info)
            || id.getName().startsWith("ossSpinSvcVer") //Info type metrics (aka for version info)
            || id.getName().startsWith("armSpinSvcVer") //Info type metrics (aka for version info)
            || id.getName().startsWith("exception") //any exception metrics
            || id.getName().startsWith("resillience4j") //circuit breaker metrics
            || id.getName().startsWith("jvm") //jvm metrics
            || id.getName().startsWith("apm") // NewRelic metrics
            || id.getName().startsWith("newrelic") // NewRelic metrics
            || id.getName().startsWith("stage") // Orca stage metrics
            || id.getName().startsWith("queue") // Orca related metrics
            || id.getName().startsWith("threadpool") //Orca metrics;
            || id.getName().startsWith("execution") // Clouddriver metrics
            || id.getName().startsWith("rateLimitThrottling") // Clouddriver metrics
            || id.getName().startsWith("aws") // CloudDriver AWS related metrics aka aws.request.throttling
            || id.getName().startsWith("bakes") // Rosco metrics
            || id.getName().startsWith("storage") // front50 metrics - cache size & age
            || id.getName().startsWith("google.storage") // front50 metrics - cache size & age
            || id.getName().startsWith("pollling") // Igor metrics
            || id.getName().startsWith("kubesvc") // Armory Agent metrics
            || "k8s.clusterVersion".equals(id.getName()) // Igor metrics
    ;
  }

}
