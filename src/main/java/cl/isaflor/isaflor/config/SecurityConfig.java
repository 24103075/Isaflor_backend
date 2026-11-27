
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

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

            // Desactivar CSRF para autenticacion JWT
            .csrf(csrf -> csrf.disable())

            // No mantener sesiones en el servidor
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Desactivar autenticacion por formulario y HTTP Basic
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            // Configurar respuestas de autenticacion y permisos
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) ->
                    response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "No autenticado"))
                .accessDeniedHandler((request, response, exception) ->
                    response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Acceso denegado")))

            // Configurar permisos de acceso
            .authorizeHttpRequests(auth -> auth

                // Permitir el procesamiento interno de errores
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                // Permitir solicitudes de verificacion CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Registro y login publicos
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/auth/registro",
                    "/api/v1/auth/login").permitAll()

                // Consulta publica de comunas
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/comunas",
                    "/api/v1/comunas/**").permitAll()

                // Consulta publica de regiones
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/regiones",
                    "/api/v1/regiones/**").permitAll()

                // Administracion de regiones
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/regiones",
                    "/api/v1/regiones/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,
                    "/api/v1/regiones",
                    "/api/v1/regiones/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/regiones",
                    "/api/v1/regiones/**").hasRole("ADMINISTRADOR")

                // Consulta publica de categorias
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/categorias",
                    "/api/v1/categorias/**").permitAll()

                // Consulta publica de tipos de documento
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/tipos-documento",
                    "/api/v1/tipos-documento/**").permitAll()

                // Administracion de tipos de documento
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/tipos-documento",
                    "/api/v1/tipos-documento/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,
                    "/api/v1/tipos-documento",
                    "/api/v1/tipos-documento/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/tipos-documento",
                    "/api/v1/tipos-documento/**").hasRole("ADMINISTRADOR")

                // Consulta de pedidos: cliente y administrador
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/pedidos",
                    "/api/v1/pedidos/**").hasAnyRole("CLIENTE", "ADMINISTRADOR")

                // Crear pedidos: cliente autenticado
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/pedidos",
                    "/api/v1/pedidos/**").hasRole("CLIENTE")

                // Cambiar el estado de pedidos: solo administrador
                .requestMatchers(HttpMethod.PATCH,
                    "/api/v1/pedidos",
                    "/api/v1/pedidos/**").hasRole("ADMINISTRADOR")

                // Eliminar pedidos: el servicio valida la propiedad y las relaciones
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/pedidos",
                    "/api/v1/pedidos/**").hasAnyRole("CLIENTE", "ADMINISTRADOR")

                // Administracion de categorias
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/categorias",
                    "/api/v1/categorias/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,
                    "/api/v1/categorias",
                    "/api/v1/categorias/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/categorias",
                    "/api/v1/categorias/**").hasRole("ADMINISTRADOR")

                // Monitoreo de UptimeRobot
                .requestMatchers("/actuator/health").permitAll()

                // Acceso publico a Swagger
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**").permitAll()

                // Operaciones administrativas de usuarios
                .requestMatchers(
                    "/api/v1/usuarios",
                    "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")

                // Consulta publica de productos
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/productos",
                    "/api/v1/productos/**").permitAll()

                // Crear productos: solo administrador
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/productos",
                    "/api/v1/productos/**").hasRole("ADMINISTRADOR")

                // Actualizar productos: solo administrador
                .requestMatchers(HttpMethod.PUT,
                    "/api/v1/productos",
                    "/api/v1/productos/**").hasRole("ADMINISTRADOR")

                // Gestionar ofertas: solo administrador
                .requestMatchers(HttpMethod.PATCH,
                    "/api/v1/productos/*/oferta")
                    .hasRole("ADMINISTRADOR")

                // Eliminar productos: solo administrador
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/productos",
                    "/api/v1/productos/**").hasRole("ADMINISTRADOR")

                // Consulta publica de metodos de envio
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/metodos-envio",
                    "/api/v1/metodos-envio/**").permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/metodos-envio",
                    "/api/v1/metodos-envio/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,
                    "/api/v1/metodos-envio",
                    "/api/v1/metodos-envio/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/metodos-envio",
                    "/api/v1/metodos-envio/**").hasRole("ADMINISTRADOR")

                // Consulta publica de metodos de pago
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/metodos-pago",
                    "/api/v1/metodos-pago/**").permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/metodos-pago",
                    "/api/v1/metodos-pago/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,
                    "/api/v1/metodos-pago",
                    "/api/v1/metodos-pago/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/metodos-pago",
                    "/api/v1/metodos-pago/**").hasRole("ADMINISTRADOR")

                // Consulta propia de pagos para clientes y consulta global para administradores
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/pagos",
                    "/api/v1/pagos/**").hasAnyRole("CLIENTE", "ADMINISTRADOR")

                // Crear pagos: solo clientes
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/pagos",
                    "/api/v1/pagos/**").hasRole("CLIENTE")

                // Cambiar estados de pago: solo administradores
                .requestMatchers(HttpMethod.PATCH,
                    "/api/v1/pagos",
                    "/api/v1/pagos/**").hasRole("ADMINISTRADOR")

                // El handler de DELETE solo devuelve 409 y nunca elimina datos
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/pagos",
                    "/api/v1/pagos/**").permitAll()

                // Consulta publica de imagenes
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/imagenes",
                    "/api/v1/imagenes/**").permitAll()

                // Subir imagenes: solo administrador
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/imagenes/**").hasRole("ADMINISTRADOR")

                // Eliminar imagenes: solo administrador
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/imagenes/**").hasRole("ADMINISTRADOR")

                // Lectura publica de imagenes de blogs
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/imagenes-blog",
                    "/api/v1/imagenes-blog/**").permitAll()

                // Gestion de imagenes de blogs
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/imagenes-blog",
                    "/api/v1/imagenes-blog/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/imagenes-blog",
                    "/api/v1/imagenes-blog/**").hasRole("ADMINISTRADOR")

                // Consulta publica de videos asociados a blogs publicados
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/videos",
                    "/api/v1/videos/**").permitAll()

                // Gestion de videos: solo administrador
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/videos",
                    "/api/v1/videos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,
                    "/api/v1/videos",
                    "/api/v1/videos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/videos",
                    "/api/v1/videos/**").hasRole("ADMINISTRADOR")

                // Lectura publica de blogs
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/blogs",
                    "/api/v1/blogs/**").permitAll()

                // Gestion de blogs
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/blogs",
                    "/api/v1/blogs/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,
                    "/api/v1/blogs",
                    "/api/v1/blogs/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PATCH,
                    "/api/v1/blogs",
                    "/api/v1/blogs/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/blogs",
                    "/api/v1/blogs/**").hasRole("ADMINISTRADOR")

                // Gestion de detalles de pedidos
                .requestMatchers(
                    "/api/v1/detalles-pedido",
                    "/api/v1/detalles-pedido/**")
                    .hasAnyRole("CLIENTE", "ADMINISTRADOR")

                // Bloquear rutas que aun no hemos configurado
                .anyRequest().denyAll()
            )

            // Agregar filtro JWT antes del filtro de autenticacion
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

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
