package vn.iotstar.btweb10.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    /** Token lifetime in milliseconds, from security.jwt.expiration-time (JWT_EXPIRATION). */
    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    private SecretKey signInKey;

    /** Fail fast on a missing/weak secret instead of failing on first login. */
    @PostConstruct
    void init() {
        if (jwtExpiration <= 0) {
            throw new IllegalStateException("security.jwt.expiration-time must be > 0 (milliseconds)");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (RuntimeException e) {
            throw new IllegalStateException("security.jwt.secret-key (JWT_SECRET) must be a Base64 string", e);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "security.jwt.secret-key (JWT_SECRET) must decode to at least 32 bytes for HS256");
        }
        this.signInKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /** Explicit lifetime (ms). Used by tests to mint already-expired tokens. */
    public String generateToken(UserDetails userDetails, long expirationMillis) {
        return buildToken(new HashMap<>(), userDetails, expirationMillis);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("roles", userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList());
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(signInKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /** Verifies signature and expiry; throws JwtException subclasses (Signature/Expired/Malformed...). */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signInKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
