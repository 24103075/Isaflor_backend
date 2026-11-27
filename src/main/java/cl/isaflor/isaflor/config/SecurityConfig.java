
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

import jakarta.servlet.DispatcherType;

@Configuration
public class SecurityConfig {

    // Proteccion de contrasenas mediante BCrypt

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // Configuracion de seguridad

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Desactivar CSRF para la futura autenticacion JWT
            .csrf(csrf -> csrf.disable())

            // No mantener sesiones en el servidor
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Configurar permisos de acceso
            .authorizeHttpRequests(auth -> auth

                // Permitir el procesamiento interno de errores
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                // Permitir solicitudes de verificacion CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Registro publico de clientes
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/auth/registro").permitAll()

                // Consulta publica de comunas
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/comunas",
                        "/api/v1/comunas/**").permitAll()

                // Consulta publica de categorias
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/categorias").permitAll()

                // Monitoreo de UptimeRobot
                .requestMatchers("/actuator/health").permitAll()

                // Permitir acceso publico a Swagger
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                ).permitAll()

                // Operaciones administrativas de usuarios
                .requestMatchers("/api/v1/usuarios",
                        "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")

                // Consulta publica de productos
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/productos",
                        "/api/v1/productos/**").permitAll()

                // Administracion de productos y ofertas
                .requestMatchers("/api/v1/productos",
                        "/api/v1/productos/**").hasRole("ADMINISTRADOR")

                // Bloquear rutas que aun no hemos configurado
                .anyRequest().denyAll()
            );

        return http.build();
    }


    // Configuracion CORS

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Autorizar la comunicacion con React
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:8080"
        ));

        // Metodos HTTP permitidos
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // Encabezados permitidos
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
