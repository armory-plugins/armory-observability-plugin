package io.armory.plugin.observability;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArmoryEnvironmentMetadata {
    private final String applicationName;
    private final String armoryAppVersion;
    private final String ossAppVersion;
    private final String spinnakerRelease;
    private final String customerEnvId;
    private final String customerEnvName;
    private final String customerName;
}
