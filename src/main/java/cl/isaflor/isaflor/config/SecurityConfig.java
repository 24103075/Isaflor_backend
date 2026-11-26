
package cl.isaflor.isaflor.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    // Protección de contraseñas mediante BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configuración de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Autoriza la comunicación entre frontend y backend
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Desactiva CSRF para nuestra futura autenticación JWT
            // mediante encabezados, sin cookies de autenticación
            .csrf(csrf -> csrf.disable())

            // Evita mantener sesiones de autenticación en el servidor
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Configuración de permisos de acceso
            .authorizeHttpRequests(auth -> auth

                // Permitir solicitudes de verificación CORS
                .requestMatchers(HttpMethod.OPTIONS,
                        "/**").permitAll()

                // Registro público de clientes
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/auth/registro").permitAll()

                // Consulta pública de comunas
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/comunas",
                        "/api/v1/comunas/**").permitAll()

                // Monitoreo del backend mediante UptimeRobot
                .requestMatchers("/actuator/health").permitAll()

                // Operaciones administrativas de usuarios
                .requestMatchers(
                        "/api/v1/usuarios",
                        "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")

                // Bloquear rutas que aún no hemos configurado
                .anyRequest().denyAll()
            );

        return http.build();
    }

    // Configuración CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Orígenes autorizados para consumir la API
        config.setAllowedOrigins(List.of(
                "http://localhost:5173"
        ));

        // Métodos HTTP permitidos por CORS
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        // Encabezados HTTP permitidos
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
