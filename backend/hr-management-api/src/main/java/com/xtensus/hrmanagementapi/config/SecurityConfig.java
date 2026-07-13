package com.xtensus.hrmanagementapi.config;

import com.xtensus.hrmanagementapi.security.jwt.JwtAccessDeniedHandler;
import com.xtensus.hrmanagementapi.security.jwt.JwtAuthenticationEntryPoint;
import com.xtensus.hrmanagementapi.security.jwt.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                                .requestMatchers(HttpMethod.GET, "/api/departments/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/departments").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/positions/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/positions").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/positions/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/positions/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/leave-types/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/leave-types").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/leave-types/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/leave-types/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/users/*/team").hasAnyRole("MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/users/role/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/users/department/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers("/api/users/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/leave-balances/user/**").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
                                .requestMatchers("/api/leave-balances/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers(HttpMethod.POST, "/api/leave-requests").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/leave-requests").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/leave-requests/requester/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/leave-requests/approver/**").hasAnyRole("MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/api/leave-requests/*/approve").hasAnyRole("MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/api/leave-requests/*/reject").hasAnyRole("MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/leave-requests/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/leave-requests/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/leave-requests/**").authenticated()

                                .requestMatchers(HttpMethod.POST, "/api/medical-documents/upload/**").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/medical-documents/download/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/medical-documents/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/medical-documents/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers("/api/notifications/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));
        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
