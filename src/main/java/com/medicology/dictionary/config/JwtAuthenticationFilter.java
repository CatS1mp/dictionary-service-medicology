package com.medicology.dictionary.config;

import com.medicology.dictionary.security.jwt.JWTDecoder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTDecoder jwtDecoder;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userIdentifier; // Thông tin định danh từ JWT (email hoặc username)

        // 1. Kiểm tra JWT có trong header hay không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Trích xuất JWT từ Header (bỏ qua chuỗi "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // Trích xuất identifier từ JWT (hiện tại JWTDecoder đang gọi extractEmail để
            // lấy subject)
            userIdentifier = jwtDecoder.extractEmail(jwt);

            // 3. Nếu có thông tin và chưa xác thực thì tiến hành xác thực
            if (userIdentifier != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Trong dictionary Service, ta không cần truy vấn DB User nữa, chỉ cần trích xuất email/id từ Token là đủ xác thực
                // Kiểm tra tính hợp lệ của token
                if (jwtDecoder.isTokenValid(jwt, "access")) {
                    // 4. Khởi tạo đối tượng xác thực với role từ JWT
                    String role = jwtDecoder.extractRole(jwt);
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    if ("ADMIN".equals(role)) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userIdentifier,
                            null,
                            authorities
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. Báo danh với Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Có thể token hết hạn hoặc sai định dạng, catch exception và để filter tiếp
            // tục (sẽ bị chặn ở các filter sau nếu endpoint requires bảo mật)
            throw new RuntimeException("Lỗi xác thực JWT: " + e.getMessage());
        }

        // 6. Chuyển request đến bộ lọc tiếp theo
        filterChain.doFilter(request, response);
    }
}
