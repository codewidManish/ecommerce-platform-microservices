package com.ecommerce.gateway.filter;

import com.ecommerce.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    public AuthenticationFilter() {
        super(Config.class);
        this.jwtUtil = null;
        this.routeValidator = null;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (routeValidator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);
                try {
                    if (!jwtUtil.validateToken(token)) {
                        return onError(exchange, "Invalid or Expired Token", HttpStatus.UNAUTHORIZED);
                    }

                    String username = jwtUtil.extractUsername(token);
                    String role     = jwtUtil.extractRole(token);
                    String userId   = jwtUtil.extractUserId(token);

                    // Forward user identity to downstream services via trusted headers
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-User-Name", username)
                            .header("X-User-Role", role != null ? role : "")
                            .header("X-User-Id",   userId != null ? userId : "")
                            .build();

                    log.debug("Authenticated user: {}, role: {}, id: {}", username, role, userId);
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (Exception e) {
                    log.error("JWT processing error: {}", e.getMessage());
                    return onError(exchange, "Token processing error", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        log.warn("Gateway auth error: {} - {}", status, message);
        return response.setComplete();
    }

    public static class Config {
        // Put configuration properties here
    }
}
