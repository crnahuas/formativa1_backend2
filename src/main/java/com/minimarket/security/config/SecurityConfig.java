package com.minimarket.security.config;

import com.minimarket.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para desarrollo y API REST
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // Permitir consola H2
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/public/**", "/h2-console/**", "/favicon.ico").permitAll()
                        
                        // Control de Usuarios (Solo ADMIN/GERENTE)
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        
                        // Productos y Categorías (Lectura para todos, Escritura solo ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**", "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**", "/api/categorias/**").hasRole("ADMIN")
                        
                        // Ventas, Detalles e Inventario (EMPLEADO y ADMIN)
                        .requestMatchers("/api/ventas/**", "/api/detalle-ventas/**", "/api/inventario/**").hasAnyRole("EMPLEADO", "ADMIN")
                        
                        // Carrito (CLIENTE, EMPLEADO y ADMIN)
                        .requestMatchers("/api/carrito/**").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                        
                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}) // Habilitar Basic Auth para pruebas rápidas
                .formLogin(form -> form
                        .defaultSuccessUrl("/public/hola", true) // Redirigir después del login
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/public/hola")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Configuración de encriptación de contraseñas
    }
}

