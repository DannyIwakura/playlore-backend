package com.playrole.config;

import com.playrole.chat.auth.CharacterSessionFilter;
import com.playrole.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;

import tools.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CharacterSessionFilter characterSessionFilter;
    private final ObjectMapper objectMapper;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CharacterSessionFilter characterSessionFilter,
                          ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.characterSessionFilter = characterSessionFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
        		// Endpoints públicos
        	    .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
        	    .requestMatchers(HttpMethod.POST, "/usuarios/login").permitAll()
        	    .requestMatchers(HttpMethod.POST, "/usuarios/google-login").permitAll()

        	    // CORS preflight para cualquier origen (PNA requests incluidos)
        	    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

        	    // Recursos estáticos
        	    .requestMatchers("/images/**").permitAll()
        	    .requestMatchers("/uploads/**").permitAll()

        	    // WebSocket endpoint (autenticación vía JwtHandshakeInterceptor)
        	    .requestMatchers("/ws/**").permitAll()

        	    // Para iniciar sesión de personaje, se necesita el user JWT
        	    .requestMatchers(HttpMethod.POST, "/personajes/sesion/iniciar").authenticated()

        	    // Cerrar sesión via beacon (sin headers de auth, el token va en el body)
        	    .requestMatchers(HttpMethod.POST, "/personajes/sesion/cerrar-beacon").permitAll()

        	    // Canales y chat requieren autenticación (user JWT o session JWT)
        	    .requestMatchers("/canales/**").authenticated()
        	    .requestMatchers("/chat/**").authenticated()
        	    .requestMatchers("/personajes/sesion/**").authenticated()
        	    .requestMatchers("/personajes/*/online").authenticated()

        	    // Categorías: GET para cualquier autenticado, el resto solo ADMIN
        	    .requestMatchers(HttpMethod.GET, "/categorias/**").authenticated()
        	    .requestMatchers("/categorias/**").hasRole("ADMIN")

        	    // Usuarios: GETs específicos para autenticados, el resto solo ADMIN
        	    .requestMatchers(HttpMethod.GET, "/usuarios/buscar").authenticated()
        	    .requestMatchers(HttpMethod.GET, "/usuarios/{id}").authenticated()
        	    .requestMatchers(HttpMethod.GET, "/usuarios/{id}/amigos").authenticated()
        	    .requestMatchers(HttpMethod.DELETE, "/usuarios/{userId}/amigos/{amigoId}").authenticated() 
        	    .requestMatchers(HttpMethod.PUT, "/usuarios/{id}/rol").hasRole("ADMIN")
          	    .requestMatchers(HttpMethod.PUT, "/usuarios/{id}").authenticated()
          	    .requestMatchers(HttpMethod.PUT, "/usuarios/{id}/ultima-conexion").authenticated()
          	    .requestMatchers("/usuarios/**").hasRole("ADMIN")

        	    // Denuncias: crear requiere autenticación (user JWT o session JWT),
        	    // el resto (listar/resolver) solo ADMIN o MOD
        	    .requestMatchers(HttpMethod.POST, "/denuncias").authenticated()
        	    .requestMatchers("/denuncias/**").hasAnyRole("ADMIN", "MOD")

        	    // Moderación (baneos globales): solo ADMIN o MOD
        	    .requestMatchers("/moderacion/**").hasAnyRole("ADMIN", "MOD")

        	    // Todo lo demás requiere autenticación
        	    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    escribirJson(response, jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED,
                            "No autenticado o sesión caducada"))
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String mensaje = (accessDeniedException instanceof com.playrole.exception.AccessDeniedException)
                            ? accessDeniedException.getMessage()
                            : "No tienes permisos para realizar esta acción";
                    escribirJson(response, jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN, mensaje);
                })
            )

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(characterSessionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    private void escribirJson(jakarta.servlet.http.HttpServletResponse response, int status, String mensaje)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("error", mensaje));
    }

    //necesario para encriptar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(config::addAllowedOrigin);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}