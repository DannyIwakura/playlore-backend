package com.playrole.config;

import com.playrole.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
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

        	    // CORS preflight para cualquier origen (PNA requests incluidos)
        	    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

        	    // Recursos estáticos
        	    .requestMatchers("/images/**").permitAll()
        	    .requestMatchers("/uploads/**").permitAll()

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

        	    // Todo lo demás requiere autenticación
        	    .anyRequest().authenticated()
            )
            
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(pnaHeaderFilter(), BasicAuthenticationFilter.class);

        return http.build();
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
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public OncePerRequestFilter pnaHeaderFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response,
                    jakarta.servlet.FilterChain chain)
                    throws jakarta.servlet.ServletException, IOException {
                response.addHeader("Access-Control-Allow-Private-Network", "true");
                chain.doFilter(request, response);
            }
        };
    }
}