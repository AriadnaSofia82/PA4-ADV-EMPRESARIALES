package com.isil.edu.pe.spedidos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/assets/**").permitAll()

                        .requestMatchers("/h2-console/**").denyAll()

                        // Registrar pedidos
                        .requestMatchers(HttpMethod.POST, "/pedidos")
                        .hasRole("OPERADOR")

                        // Consultar pedidos
                        .requestMatchers(HttpMethod.GET, "/pedidos")
                        .hasAnyRole("OPERADOR", "CONSULTA")

                        // Revisión de pedidos: solamente operador
                        .requestMatchers(
                                HttpMethod.GET,
                                "/revision",
                                "/revision/**"
                        ).hasRole("OPERADOR")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/revision",
                                "/revision/**"
                        ).hasRole("OPERADOR")

                        // Reportes
                        .requestMatchers(HttpMethod.GET, "/reportes/**")
                        .hasAnyRole("OPERADOR", "CONSULTA")

                        // API de pedidos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/pedidos",
                                "/api/v1/pedidos/**"
                        ).hasAnyRole("OPERADOR", "CONSULTA")

                        // Anulaciones
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/anulaciones"
                        ).hasRole("OPERADOR")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/anulaciones"
                        ).hasAnyRole("OPERADOR", "CONSULTA")

                        // Cualquier otra operación requiere autenticación
                        .anyRequest().authenticated()
                )

                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder encoder,

            @Value("${demo.security.operador-password}")
            String operadorPassword,

            @Value("${demo.security.consulta-password}")
            String consultaPassword) {

        UserDetails operador = User.withUsername("operador")
                .password(encoder.encode(operadorPassword))
                .roles("OPERADOR")
                .build();

        UserDetails consulta = User.withUsername("consulta")
                .password(encoder.encode(consultaPassword))
                .roles("CONSULTA")
                .build();

        return new InMemoryUserDetailsManager(
                operador,
                consulta
        );
    }
}
