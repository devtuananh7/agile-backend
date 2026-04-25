package vn.hust.agilechatbotbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that validates API key for public endpoints (/api/v1/public/**).
 * Replaces Firebase authentication for anonymous access.
 *
 * Flow:
 * 1. Skip if request path is NOT /api/v1/public/**
 * 2. Extract X-API-Key header
 * 3. Compare with configured caretalk.public.api-key
 * 4. Reject with 401 if missing or invalid
 */
@Component
@Slf4j
public class PublicApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String PUBLIC_PATH_PREFIX = "/api/v1/public/";

    @Value("${caretalk.public.api-key:}")
    private String configuredApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Only apply to /api/v1/public/** paths
        if (!requestPath.startsWith(PUBLIC_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Missing API key for public endpoint: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing API key\"}");
            return;
        }

        log.info("API key check — received: [{}] (len={}), configured: [{}] (len={})",
                apiKey, apiKey.length(), configuredApiKey, configuredApiKey.length());
        if (!apiKey.equals(configuredApiKey)) {
            log.warn("Invalid API key for public endpoint: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid API key\"}");
            return;
        }

        log.debug("API key validated for public endpoint: {}", requestPath);
        filterChain.doFilter(request, response);
    }
}
