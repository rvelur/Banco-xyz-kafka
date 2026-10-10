package cl.duoc.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SecurityConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Canales abiertos para pruebas o según esquema de autenticación
                .requestMatchers("/api/bff/atm/**", "/api/bff/mobile/**", "/api/bff/web/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}