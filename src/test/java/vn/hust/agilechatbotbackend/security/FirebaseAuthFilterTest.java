package vn.hust.agilechatbotbackend.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.hust.agilechatbotbackend.entity.User;
import vn.hust.agilechatbotbackend.enums.AccountStatus;
import vn.hust.agilechatbotbackend.enums.Role;
import vn.hust.agilechatbotbackend.service.UserSyncService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseAuthFilterTest {

    @Mock
    private UserSyncService userSyncService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private FirebaseAuthFilter firebaseAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChainWhenNoAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        firebaseAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldContinueFilterChainWhenNonBearerAuth() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        firebaseAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldSetSecurityContextForValidToken() throws Exception {
        // Given
        String token = "valid-firebase-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        FirebaseToken decodedToken = mock(FirebaseToken.class);
        when(decodedToken.getUid()).thenReturn("firebase-uid-123");
        when(decodedToken.getEmail()).thenReturn("test@example.com");
        when(decodedToken.getName()).thenReturn("Test User");
        when(decodedToken.getClaims()).thenReturn(Map.of(
                "firebase", Map.of("sign_in_provider", "google.com")
        ));

        User user = User.builder()
                .id(UUID.randomUUID())
                .firebaseUid("firebase-uid-123")
                .email("test@example.com")
                .role(Role.PATIENT)
                .status(AccountStatus.ACTIVE)
                .build();

        when(userSyncService.syncUser("firebase-uid-123", "test@example.com", "Test User", "google.com"))
                .thenReturn(user);

        try (MockedStatic<FirebaseAuth> firebaseAuthMocked = mockStatic(FirebaseAuth.class)) {
            FirebaseAuth mockAuth = mock(FirebaseAuth.class);
            firebaseAuthMocked.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
            when(mockAuth.verifyIdToken(token)).thenReturn(decodedToken);

            // When
            firebaseAuthFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

            CustomUserDetails principal = (CustomUserDetails)
                    SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            assertThat(principal.getFirebaseUid()).isEqualTo("firebase-uid-123");
            assertThat(principal.getEmail()).isEqualTo("test@example.com");
            assertThat(principal.getRole()).isEqualTo(Role.PATIENT);
        }
    }

    @Test
    void shouldReturn403ForBlockedUser() throws Exception {
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        FirebaseToken decodedToken = mock(FirebaseToken.class);
        when(decodedToken.getUid()).thenReturn("blocked-uid");
        when(decodedToken.getEmail()).thenReturn("blocked@example.com");
        when(decodedToken.getName()).thenReturn("Blocked User");
        when(decodedToken.getClaims()).thenReturn(Map.of());

        User blockedUser = User.builder()
                .id(UUID.randomUUID())
                .firebaseUid("blocked-uid")
                .email("blocked@example.com")
                .role(Role.PATIENT)
                .status(AccountStatus.BLOCKED)
                .build();

        when(userSyncService.syncUser(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(blockedUser);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        try (MockedStatic<FirebaseAuth> firebaseAuthMocked = mockStatic(FirebaseAuth.class)) {
            FirebaseAuth mockAuth = mock(FirebaseAuth.class);
            firebaseAuthMocked.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
            when(mockAuth.verifyIdToken(token)).thenReturn(decodedToken);

            firebaseAuthFilter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(filterChain, never()).doFilter(request, response);
            assertThat(sw.toString()).contains("Account is disabled");
        }
    }

    @Test
    void shouldReturn401ForInvalidToken() throws Exception {
        String token = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        try (MockedStatic<FirebaseAuth> firebaseAuthMocked = mockStatic(FirebaseAuth.class)) {
            FirebaseAuth mockAuth = mock(FirebaseAuth.class);
            firebaseAuthMocked.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
            FirebaseAuthException mockException = mock(FirebaseAuthException.class);
            when(mockException.getMessage()).thenReturn("Invalid token");
            when(mockException.getAuthErrorCode()).thenReturn(null);
            when(mockAuth.verifyIdToken(token)).thenThrow(mockException);

            firebaseAuthFilter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(request, response);
            assertThat(sw.toString()).contains("Invalid token");
        }
    }
}
