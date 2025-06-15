package satisfactionclient.Enquete_service;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String SECRET = "GnzzsOitlN5w0CjaYODCk62v+/WmDl2HApnaRb/VEJg=";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/enquete/respond/**").permitAll()
                        .requestMatchers("/admin/enquetes/**", "/admin/enquetes","/admin/enquetes/create-ia", "/admin/enquetes/{id}", "/admin/enquetes/update/{id}").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .csrf(csrf -> csrf.disable()); // désactive CSRF pour éviter les erreurs sur POST en CORS

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] secretBytes = Base64.getDecoder().decode(SECRET);
        SecretKeySpec secretKey = new SecretKeySpec(secretBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }


}