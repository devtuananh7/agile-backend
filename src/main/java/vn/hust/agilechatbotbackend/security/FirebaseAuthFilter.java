package vn.hust.agilechatbotbackend.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.hust.agilechatbotbackend.entity.User;
import vn.hust.agilechatbotbackend.enums.AccountStatus;
import vn.hust.agilechatbotbackend.service.UserSyncService;

import java.io.IOException;

/**
 * Spring Security filter that verifies Firebase ID Tokens.
 * Replaces the legacy JwtAuthenticationFilter.
 *
 * Flow:
 * 1. Extract Bearer token from Authorization header
 * 2. Verify with FirebaseAuth.verifyIdToken()
 * 3. Auto-sync user to PostgreSQL (via UserSyncService)
 * 4. Set SecurityContext with CustomUserDetails
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserSyncService userSyncService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 1. Verify Firebase ID Token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();
            String provider = "unknown";
            Object firebaseClaim = decodedToken.getClaims().get("firebase");
            if (firebaseClaim instanceof java.util.Map<?, ?> firebaseMap) {
                Object signInProvider = firebaseMap.get("sign_in_provider");
                if (signInProvider instanceof String s) {
                    provider = s;
                }
            }

            log.debug("Firebase token verified: uid={}, email={}, provider={}", uid, email, provider);

            // 2. Auto-sync user to PostgreSQL
            User user = userSyncService.syncUser(uid, email, name, provider);

            // 3. Check if account is disabled
            if (user.getStatus() == AccountStatus.INACTIVE || user.getStatus() == AccountStatus.BLOCKED) {
                log.warn("Disabled account attempted access: uid={}, status={}", uid, user.getStatus());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Account is disabled\"}");
                return;
            }

            // 4. Build CustomUserDetails and set SecurityContext
            CustomUserDetails userDetails = CustomUserDetails.build(user);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, decodedToken, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (FirebaseAuthException e) {
            log.warn("Firebase token verification failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            String errorMsg = e.getAuthErrorCode() != null &&
                    e.getAuthErrorCode().name().contains("EXPIRED")
                    ? "Token expired" : "Invalid token";

            response.getWriter().write("{\"error\": \"" + errorMsg + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
