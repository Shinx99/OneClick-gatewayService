package com.oneClick.gatewayService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Slf4j
public class SecurityConfig {

    @Value("${CORS_ALLOWED_HEADERS:*}")
    private String corsAllowedHeaders;

    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:3000}")
    private String corsAllowedOrigins;

    @Value("${CORS_ALLOWED_METHODS:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String corsAllowedMethods;

    @Value("${CORS_ALLOW_CREDENTIALS:true}")
    private boolean corsAllowCredentials;

    @Value("${CORS_MAX_AGE:3600}")
    private long corsMaxAge;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ← CORS từ env
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/ws/**").permitAll()
                        .pathMatchers("/api/auth/**", "/actuator/**").permitAll()

                                // ========== JOB APPLICATION APIs (CẦN THÊM) ==========
                                // 1. Employer: Get application stats
                                .pathMatchers(HttpMethod.GET, "/api/employer/jobs/{jobId}/applications/stats")
                                .hasAuthority("ROLE_recruiter")

                                // 2. Employer: Get application detail by applicationId
                                .pathMatchers(HttpMethod.GET, "/api/employer/applications/{applicationId}")
                                .hasAuthority("ROLE_recruiter")

                                // 3. Employer: Get application history
                                .pathMatchers(HttpMethod.GET, "/api/employer/applications/{applicationId}/history")
                                .hasAuthority("ROLE_recruiter")

                                // 4. Employer: Update application status (API mới dùng applicationId)
                                .pathMatchers(HttpMethod.PATCH, "/api/employer/applications/{applicationId}/status")
                                .hasAuthority("ROLE_recruiter")

                                // 5. Employer: Schedule interview
                                .pathMatchers(HttpMethod.POST, "/api/employer/applications/{applicationId}/schedule-interview")
                                .hasAuthority("ROLE_recruiter")

                                // 6. Employer: Cancel interview
                                .pathMatchers(HttpMethod.DELETE, "/api/employer/schedule-interview/{scheduleId}")
                                .hasAuthority("ROLE_recruiter")

                                // 7. Employer: Get my jobs
                                .pathMatchers(HttpMethod.GET, "/api/employer/jobs")
                                .hasAuthority("ROLE_recruiter")

                                // ========== NOTIFICATION APIs (CẦN THÊM) ==========
                                // 8. Candidate: Get my notifications
                                .pathMatchers(HttpMethod.GET, "/api/notifications/my")
                                .hasAuthority("ROLE_candidate")

                                // 9. Candidate: Get unread count
                                .pathMatchers(HttpMethod.GET, "/api/notifications/unread-count")
                                .hasAuthority("ROLE_candidate")

                                // 10. Candidate: Mark notification as read
                                .pathMatchers(HttpMethod.PATCH, "/api/notifications/{id}/read")
                                .hasAuthority("ROLE_candidate")

                                // 11. Employer: Get employer notifications
                                .pathMatchers(HttpMethod.GET, "/api/employer/notifications")
                                .hasAuthority("ROLE_recruiter")

                                // 12. Employer: Get employer unread count
                                .pathMatchers(HttpMethod.GET, "/api/employer/notifications/unread-count")
                                .hasAuthority("ROLE_recruiter")

                        // ========== AI CV MATCHER APIs (THÊM MỚI) ==========
                        // Match với resume đã lưu (candidate tự match CV của mình)
                        .pathMatchers(HttpMethod.POST, "/api/ai-cv-match/resume/{resumeId}/job/{jobId}")
                        .hasAuthority("ROLE_candidate")

                        // Match với file mới upload (candidate upload CV mới để test)
                        .pathMatchers(HttpMethod.POST, "/api/ai-cv-match/new/{jobId}")
                        .hasAuthority("ROLE_candidate")

                        // Match với S3 URL (cho external CV, có thể dùng cho cả candidate và recruiter)
                        .pathMatchers(HttpMethod.POST, "/api/ai-cv-match/s3/{jobId}")
                        .authenticated()

                        // Match với cached data (fast match - candidate)
                        .pathMatchers(HttpMethod.POST, "/api/ai-cv-match/cache/{jobId}")
                        .hasAuthority("ROLE_candidate")

                        // GET endpoints nếu có (ví dụ: lấy lịch sử match)
                        .pathMatchers(HttpMethod.GET, "/api/ai-cv-match/history/**")
                        .hasAuthority("ROLE_candidate")

                        // Admin endpoints (nếu cần)
                        .pathMatchers(HttpMethod.GET, "/api/ai-cv-match/admin/**")
                        .hasAuthority("ROLE_admin")

                        // ========== JOB APPLICATION APIs ==========
                        // Candidate applies for a job
                        .pathMatchers(HttpMethod.POST, "/api/jobs/apply").hasAuthority("ROLE_candidate")

                        // Check if already applied
                        .pathMatchers(HttpMethod.GET, "/api/jobs/{jobId}/check-applied").hasAuthority("ROLE_candidate")

                        // Get my applications (candidate)
                        .pathMatchers(HttpMethod.GET, "/api/applications/my-applications").hasAuthority("ROLE_candidate")

                        // Cancel application (candidate)
                        .pathMatchers(HttpMethod.DELETE, "/api/applications/{jobId}").hasAuthority("ROLE_candidate")

                        // Get application detail (candidate or employer)
                        .pathMatchers(HttpMethod.GET, "/api/applications/{jobId}/{candidateId}").authenticated()

                        // Employer: Get candidates by job
                        .pathMatchers(HttpMethod.GET, "/api/employer/jobs/{jobId}/applications").hasAuthority("ROLE_recruiter")

                        // Employer: Update application status
                        .pathMatchers(HttpMethod.PATCH, "/api/employer/applications/{jobId}/{candidateId}/status").hasAuthority("ROLE_recruiter")


                        // ========== RECRUITMENT APIs ==========
                        // Join Company Request
                        .pathMatchers(HttpMethod.POST, "/api/recruitment/company/*/join-request").hasAuthority("ROLE_recruiter")
                        .pathMatchers(HttpMethod.GET, "/api/recruitment/company/join-requests").hasAuthority("ROLE_recruiter")
                        .pathMatchers(HttpMethod.PUT, "/api/recruitment/company/join-request/*/approve").hasAuthority("ROLE_recruiter")
                        .pathMatchers(HttpMethod.PUT, "/api/recruitment/company/join-request/*/reject").hasAuthority("ROLE_recruiter")

                        .pathMatchers(HttpMethod.PUT,
                                "/api/recruitment/company/logo/upload",
                                "/api/recruitment/company/background/upload")
                        .hasAuthority("ROLE_recruiter")
                        .pathMatchers(HttpMethod.POST, "/api/recruitment/company").hasAuthority("ROLE_recruiter")
                        .pathMatchers(HttpMethod.GET, "/api/recruitment/company/**").permitAll()
                        // Job CRUD (recruiter)
                        .pathMatchers(HttpMethod.POST, "/api/recruitment/job/create").hasAuthority("ROLE_recruiter")
                        .pathMatchers(HttpMethod.GET, "/api/recruitment/job/my-jobs").hasAuthority("ROLE_recruiter")
                        .pathMatchers(HttpMethod.PUT, "/api/recruitment/job/*/image/upload").hasAuthority("ROLE_recruiter")
                        .pathMatchers(HttpMethod.PUT, "/api/recruitment/job/*").hasAuthority("ROLE_recruiter")
                        .pathMatchers(HttpMethod.DELETE, "/api/recruitment/job/*").hasAuthority("ROLE_recruiter")
                        .pathMatchers(HttpMethod.GET, "/api/recruitment/job/**").permitAll()

                        .pathMatchers("/api/recruitment/candidate/**").hasAuthority("ROLE_candidate")
                        .pathMatchers("/api/recruitment/employer/**").hasAuthority("ROLE_recruiter")
                        .pathMatchers("/api/recruitment/**").hasAuthority("ROLE_candidate")
                        .pathMatchers("/api/admin/**").hasAuthority("ROLE_admin")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> allowedOrigins = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        List<String> allowedMethods = Arrays.stream(corsAllowedMethods.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        List<String> allowedHeaders = Arrays.stream(corsAllowedHeaders.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        //config.setAllowedOrigins(allowedOrigins);
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(allowedMethods);
        config.setAllowedHeaders(allowedHeaders.isEmpty() ? List.of("*") : allowedHeaders);
        config.setExposedHeaders(List.of("Authorization", "X-Total-Count"));
        config.setAllowCredentials(corsAllowCredentials);
        config.setMaxAge(corsMaxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        converter.setPrincipalClaimName("accountId");

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}