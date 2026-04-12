package com.medicology.dictionary.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTDecoder {

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // 3. Hàm kiểm tra Token hợp lệ hay không
    public boolean isTokenValid(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Kiểm tra đúng loại token (Access vs Refresh)
            String tokenType = claims.get("type", String.class);
            return expectedType.equals(tokenType);

        } catch (ExpiredJwtException e) {
            // Token hết hạn - Log ra để debug nếu cần
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // Chữ ký sai, format sai, token rỗng...
            return false;
        }
    }
}
