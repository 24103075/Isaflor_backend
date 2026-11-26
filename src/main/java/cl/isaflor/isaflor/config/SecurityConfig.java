
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

    // Protección de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() { //Protege las contraseñas mediante BCrypt.
        return new BCryptPasswordEncoder();
    }

    // Configuración de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)  
            throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  ////Define los permisos de acceso a las APIs.

            .csrf(csrf -> csrf.disable())   //Desactiva CSRF para nuestra futura API con autenticación JWT mediante encabezados, sin cookies de autenticación.

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  //Evita mantener sesiones de autenticación en el servidor.

            .authorizeHttpRequests(auth -> auth

                // Permitir solicitudes de verificación CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  //Permite acceder sin autenticación.

                // Registro público de clientes
                .requestMatchers(HttpMethod.POST,
                        "/api/auth/registro").permitAll()

                // Consulta pública de comunas
                .requestMatchers(HttpMethod.GET,
                        "/api/comunas",
                        "/api/comunas/**").permitAll()

                // Monitoreo de UptimeRobot
                .requestMatchers("/actuator/health").permitAll()

                // Operaciones administrativas de usuarios
                .requestMatchers("/api/usuarios",
                        "/api/usuarios/**").hasRole("ADMINISTRADOR")  //Exige el rol administrativo.

                // Bloquear rutas que aún no hemos configurado
                .anyRequest().denyAll()   
            );

        return http.build();
    }

    // Configuración CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {  //	Autoriza la comunicación con React.

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

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
