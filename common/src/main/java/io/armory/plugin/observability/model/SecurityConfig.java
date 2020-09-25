package io.armory.plugin.observability.model;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

//To avoid collision with other WebSecurityConfigurerAdapters
@Order(Ordered.HIGHEST_PRECEDENCE + 27)
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final PluginConfig pluginConfig;

    public SecurityConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
       if (pluginConfig.getMetrics().getPrometheus().isEnabled()) {
           http.requestMatcher(EndpointRequest.to("aop-prometheus")).authorizeRequests((requests) ->
                   requests.anyRequest().permitAll());
       } else {
           http.requestMatcher(EndpointRequest.to("aop-prometheus")).authorizeRequests((requests) ->
                   requests.anyRequest().denyAll());
       }
    }
}
