package hu.kornel.server.infrastructure.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import hu.kornel.server.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                // Public content browsing 
                .requestMatchers(HttpMethod.GET, "/api/modules", "/api/modules/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/lessons/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/exercises/{lessonId}/{exerciseId}/validate").permitAll()

                // Public sandbox features
                .requestMatchers(HttpMethod.POST, "/api/sandbox/regex/examples").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/sandbox/examples/builtin").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/sandbox/examples/{id}").permitAll()

                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("TEACHER")

                // Teacher-only endpoints
                .requestMatchers("/api/assignments/**").hasRole("TEACHER")
                .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.POST, "/api/groups").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/groups/{groupId}").hasRole("TEACHER")

                .requestMatchers("/api/groups/{groupId}/assignments/**").hasAnyRole("STUDENT", "TEACHER")

                .requestMatchers("/api/difficulty/**").authenticated()
                .requestMatchers("/api/lessons/progress/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/lessons/{lessonId}/complete").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/lessons/{lessonId}/access").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/lessons/{lessonId}/prerequisites/check").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/exercises/{lessonId}/{exerciseId}/solution").authenticated()

                // User sandbox content - require authentication
                .requestMatchers(HttpMethod.GET, "/api/sandbox/examples/user").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/sandbox/examples/upload").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/sandbox/examples/{id}/fork").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/sandbox/examples/{id}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/sandbox/examples/{id}").authenticated()

                // Group management - authenticated users
                .requestMatchers("/api/groups/**").authenticated()

                // Auth me endpoint
                .requestMatchers("/api/auth/me").authenticated()

                // Deny all other requests by default 
                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
