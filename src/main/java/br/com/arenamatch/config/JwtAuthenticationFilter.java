package br.com.arenamatch.config;

import br.com.arenamatch.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                Map<String, Object> payload = jwtService.validarToken(authorization.substring(7));
                String email = String.valueOf(payload.get("sub"));
                String perfil = String.valueOf(payload.get("perfil"));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + perfil))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
