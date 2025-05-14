package satisfactionclient.user_service.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import satisfactionclient.user_service.Repository.UserRepository;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    @Autowired
    private UserRepository userRepository;
    private final SecretKey key;

    @Value("${jwt.expirationMs}")
    private long expirationMs;


    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(Long userId, String username, List<String> roles) {
        return Jwts.builder()
                .subject(String.valueOf(userId)) // standard 'sub'
                .claim("username", username)
                .claim("roles", roles) // 🔥 très important pour sécurité
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }


    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(key) // `verifyWith` fonctionne maintenant correctement
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("sub", String.class); // Récupère `sub` au lieu de `subject()`
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }




}