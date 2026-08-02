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

                                .requestMatchers(HttpMethod.GET, "/api/departements/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/departements").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/departements/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/departements/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/postes/**", "/api/type-contrats/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/postes").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/postes/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/postes/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/type-contrats").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/type-contrats/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/type-contrats/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/conge-types/**", "/api/conge-demande-statuts/**", "/api/raisons/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/conge-types", "/api/conge-demande-statuts", "/api/raisons").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/conge-types/**", "/api/conge-demande-statuts/**", "/api/raisons/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/conge-types/**", "/api/conge-demande-statuts/**", "/api/raisons/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/employes/*/equipe").hasAnyRole("MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/users/role/**", "/api/employes/role/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/users/department/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers("/api/users/**", "/api/employes/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/leave-balances/user/**", "/api/conge-soldes/user/**").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
                                .requestMatchers("/api/leave-balances/**", "/api/conge-soldes/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers("/api/leave-accruals/**", "/api/acquisitions-conges/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers(HttpMethod.POST, "/api/conge-demandes-v2").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/conge-demandes-v2").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/conge-demandes-v2/employe/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/conge-demandes-v2/decideur/**").hasAnyRole("MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/conge-demandes-v2/*/approuver").hasAnyRole("MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/conge-demandes-v2/*/refuser").hasAnyRole("MANAGER", "HR", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/conge-demandes-v2/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/conge-demandes-v2/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/conge-demandes-v2/**").authenticated()

                                .requestMatchers(HttpMethod.POST, "/api/medical-documents/upload/**", "/api/certificats-medicaux/upload/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/medical-documents/download/**", "/api/certificats-medicaux/download/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/medical-documents/**", "/api/certificats-medicaux/**").hasAnyRole("HR", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/medical-documents/**", "/api/certificats-medicaux/**").hasAnyRole("HR", "ADMIN")

                                .requestMatchers("/api/notifications-v2/**").authenticated()
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





