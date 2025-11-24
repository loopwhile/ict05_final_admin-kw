package com.boot.ict05_final_admin.config.security;

import com.boot.ict05_final_admin.config.security.filter.SyncAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SyncFilterConfig {

    @Bean
    @ConditionalOnProperty(name = "sync.shared-secret")
    public SyncAuthFilter syncAuthFilter(@Value("${sync.shared-secret}") String secret) {
        return new SyncAuthFilter(secret);
    }
}
