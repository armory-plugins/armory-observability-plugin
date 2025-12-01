package io.armory.plugin.observability.model;

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    private final PluginConfig pluginConfig;

    public SecurityConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (pluginConfig.getMetrics().getPrometheus().isEnabled()) {
            http.authorizeHttpRequests(
                    authorize -> authorize
                            .requestMatchers("aop-prometheus")
                            .permitAll().anyRequest()
            );
        } else {
            http.authorizeHttpRequests(
                    authorize -> authorize
                            .requestMatchers("aop-prometheus")
                            .denyAll().anyRequest()
            );
        }
        return http.build();
    }
}
