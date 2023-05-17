package io.armory.plugin.observability.model;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//To avoid collision with other WebSecurityConfigurerAdapters
@Order(Ordered.HIGHEST_PRECEDENCE + 27)
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {
    private final PluginConfig pluginConfig;

    public SecurityConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        if (pluginConfig.getMetrics().getPrometheus().isEnabled()) {
            http.authorizeHttpRequests()
                    .requestMatchers("aop-prometheus")
                    .permitAll()
                    .anyRequest()
                    .authenticated();
            return http.build();
        } else {
            http.authorizeHttpRequests((authz) -> authz.requestMatchers("aop-prometheus").denyAll());
            return http.build();
        }
    }
}
