package com.boot.ict05_final_admin.config.security;

import com.boot.ict05_final_admin.config.security.filter.SyncAuthFilter;
import com.boot.ict05_final_admin.domain.auth.service.MemberUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


// ADD >>
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;
// << ADD

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final MemberUserDetailsService memberUserDetailsService;
    private static final RequestMatcher ADMIN_API = new AntPathRequestMatcher("/API/**");

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(memberUserDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    // 1) API 전용 체인
    @Bean
    @Order(0)
    SecurityFilterChain healthChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/health")                   // 컨텍스트(/admin)는 자동 제외된 내부 경로 기준
            .authorizeHttpRequests(a -> a.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .requestCache(c -> c.disable())
            .securityContext(sc -> sc.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http,
                                        ObjectProvider<SyncAuthFilter> syncAuthFilterProvider) throws Exception {
        http
                // ★ 여기! context-path 포함 매처로 강제
                .securityMatcher(ADMIN_API)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll()   // API는 전부 통과
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable) // ★ savedRequest로 로그인 리다이렉트 방지
                .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> res.sendError(401)));
        SyncAuthFilter filter = syncAuthFilterProvider.getIfAvailable();
        if (filter != null) {
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }


    @Bean
    @Order(1) // 체인 하나만 사용
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(ADMIN_API) // API는 CSRF 미적용
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 경로
                        .requestMatchers(
                                "/health",
                                "/login", "/login/**",
                                "/register",
                                "/css/**", "/js/**", "/images/**", "/assets/**",
                                "/api/**", "/API/**",
                                "/uploads/**", "/uploads/profile/**"
                        ).permitAll()

                        // ★ 백업 안전장치: 혹시라도 웹 체인이 잡아도 /API/**는 통과
                        .requestMatchers("/API/**").permitAll()

                        // 역할별 보호 경로
                        .requestMatchers("/store/**").hasAnyRole("FRANCHISE","ADMIN")
                        .requestMatchers("/menu/**").hasAnyRole("OPS","ADMIN")
                        .requestMatchers("/receive/**").hasAnyRole("OPS","ADMIN")
                        .requestMatchers("/inventory/**").hasAnyRole("OPS","ADMIN")
                        .requestMatchers("/material/**").hasAnyRole("OPS","ADMIN")
                        .requestMatchers("/store/material/**").hasAnyRole("OPS","ADMIN")
                        .requestMatchers("/staff/**").hasAnyRole("HR","ADMIN")
                        .requestMatchers("/analytics/**").hasAnyRole("ANALYTICS","ADMIN")
                        .requestMatchers("/member/**").hasRole("ADMIN")

                        // 나머지
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .authenticationProvider(daoAuthProvider());

        return http.build();
    }


    // ADD >>
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 개발 도메인
        cfg.setAllowedOrigins(List.of(
                "https://toastlabadmin.duckdns.org",  // ← Admin 도메인 추가
                "http://toastlabadmin.duckdns.org",   // HTTP도 추가
                "https://toastlab.duckdns.org",       // User 도메인도 추가 (필요하면)
                "http://localhost:8082", // 가맹점 프런트/게이트웨이
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost"
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setExposedHeaders(List.of("Authorization", "Content-Type", "Location"));
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        src.registerCorsConfiguration("/admin/**", cfg);
        return src;
    }
    // << ADD

    @Value("${file.upload-dir.profile}")
    private String profileUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + profileUploadDir + "/";

        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations(location);
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

}
