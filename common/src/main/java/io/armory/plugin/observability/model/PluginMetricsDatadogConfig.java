package io.armory.plugin.observability.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginMetricsDatadogConfig extends BaseMetricsIntegrationConfig {
    // New Relic Specific Settings
    @Builder.Default private boolean enabled = false;
    @Builder.Default private String apiKey = null;
    @Builder.Default private String applicationKey = null;
    //@Builder.Default private String hostTag = null;
    @Builder.Default private String uri = "https://api.datadoghq.com";
    //@Builder.Default private String descriptions = null;
    @Builder.Default private boolean enableAuditMode = false;

    // Push Registry Settings
    @Builder.Default private int stepInSeconds = 30;
    @Builder.Default private int numThreads = 2;
    @Builder.Default private int batchSize = 10000;

    @Builder.Default private int connectDurationSeconds = 5;
    @Builder.Default private int readDurationSeconds = 5;

    @Builder.Default private String proxyHost = null;
    @Builder.Default private Integer proxyPort= null;


}

