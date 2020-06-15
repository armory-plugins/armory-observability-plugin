package io.armory.plugin.observability.meterregistrycustomizer;

import io.armory.plugin.observability.model.ArmoryEnvironmentMetadata;
import io.armory.plugin.observability.model.ArmoryObservabilityPluginProperties;
import io.micrometer.core.instrument.Tag;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.info.BuildProperties;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultTagsMeterRegistryCustomizerTest {

    private static final String MOCK_APPLICATION_NAME = "mock-application-name";
    private DefaultTagsMeterRegistryCustomizer customizer;

    @Before
    public void before() {
        customizer = new DefaultTagsMeterRegistryCustomizer(new ArmoryObservabilityPluginProperties(), MOCK_APPLICATION_NAME);
    }

    @Test
    public void test_that_getBuildProperties_loads_the_props_file_properly() {
        BuildProperties props = customizer.getBuildProperties("build-info.properties");
        assertEquals(props.getName(), "clouddriver");
        assertEquals(props.getVersion(), "2.21.0");
        assertEquals(props.get("armoryVersion"), "2.21.0");
        assertEquals(props.get("spinnakerRelease"), "1.20.5");
        assertEquals(props.get("ossVersion"), "6.9.2-20200606020017");
    }

    @Test
    public void test_that_getBuildProperties_loads_the_props_file_properly_when_extra_props_are_preset() {
        BuildProperties props = customizer.getBuildProperties("build-info-with-extra.properties");
        assertEquals(props.getName(), "clouddriver");
        assertEquals(props.getVersion(), "2.21.0");
        assertEquals(props.get("armoryVersion"), "2.21.0");
        assertEquals(props.get("spinnakerRelease"), "1.20.5");
        assertEquals(props.get("ossVersion"), "6.9.2-20200606020017");
    }

    @Test
    public void test_that_getBuildProperties_doesnt_error_when_build_info_file_missing() {
        BuildProperties props = customizer.getBuildProperties("i-do-not-exist.properties");
        assertNotNull(props);
        assertNull(props.getName());
        assertNull(props.getVersion());
        assertNull(props.get("armoryVersion"));
        assertNull(props.get("spinnakerRelease"));
        assertNull(props.get("ossVersion"));
    }

    @Test
    public void test_that_getDefaultTagsAsFilteredMap_filters_null_values() {
        var res = customizer.getDefaultTagsAsFilteredMap(ArmoryEnvironmentMetadata.builder()
                .applicationName("foo")
                .customerEnvName(null)
                .build());

        assertEquals(1, res.size());
        assertEquals(res.get("applicationName"), "foo");
    }

    @Test
    public void test_that_getDefaultTags_converts_map_to_list() {
        var res = customizer.getDefaultTags(Map.of("foo", "bar"));

        assertEquals(1, res.size());
        assertEquals(res.get(0), Tag.of("foo", "bar"));
    }

    @Test
    public void test_that_getEnvironmentMetadata_reads_app_name_from_build_props() {
        var buildProps = mock(BuildProperties.class);
        when(buildProps.getName()).thenReturn("foo");
        var metadata = customizer.getEnvironmentMetadata(buildProps);
        assertEquals("foo", metadata.getApplicationName());
    }

    @Test
    public void test_that_getEnvironmentMetadata_uses_spring_app_name_props_if_build_props_null() {
        var buildProps = mock(BuildProperties.class);
        when(buildProps.getName()).thenReturn(null);
        var metadata = customizer.getEnvironmentMetadata(buildProps);
        assertEquals(MOCK_APPLICATION_NAME, metadata.getApplicationName());
    }

    @Test
    public void test_that_getEnvironmentMetadata_lastly_defaults_to_unknown() {
        var sut = new DefaultTagsMeterRegistryCustomizer(new ArmoryObservabilityPluginProperties(), null);
        var buildProps = mock(BuildProperties.class);
        when(buildProps.getName()).thenReturn(null);
        var metadata = sut.getEnvironmentMetadata(buildProps);
        assertEquals("UNKNOWN", metadata.getApplicationName());
    }
}
