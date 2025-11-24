package com.boot.ict05_final_admin.config.web;

import com.boot.ict05_final_admin.common.util.CookieUtil;
import com.boot.ict05_final_admin.config.HqProps;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@RequiredArgsConstructor
public class UserApiClientConfig {
    private final HqProps props;
    private final HttpServletRequest request;

    @Bean
    @ConditionalOnProperty(prefix = "hq", name = "user-api-base-url") // 값이 있을 때만 생성
    public RestClient userApiClient() {
        return RestClient.builder()
                .baseUrl(props.getUserApiBaseUrl())
                .requestInterceptor((req, body, ex) -> {
                    var at = CookieUtil.read(request, "accessToken");
                    if (at != null && !at.isBlank()) req.getHeaders().setBearerAuth(at);
                    return ex.execute(req, body);
                })
                .build();
    }
    @Bean
    @ConditionalOnMissingBean(RestClient.class)
    public RestClient fallbackRestClient() {
        // baseUrl 없이 생성: 실제로 호출하면 실패하므로, 연동을 안 쓸 때만 존재
        return RestClient.create();
    }
}
