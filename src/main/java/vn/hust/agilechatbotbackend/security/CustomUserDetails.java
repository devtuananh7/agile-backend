package vn.hust.agilechatbotbackend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.hust.agilechatbotbackend.entity.User;
import vn.hust.agilechatbotbackend.enums.Role;
import vn.hust.agilechatbotbackend.enums.AccountStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String firebaseUid;
    private final String email;
    private final Role role;
    private final AccountStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Build CustomUserDetails from a User entity (Firebase auth flow).
     * Password is no longer needed — Firebase handles authentication.
     */
    public static CustomUserDetails build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new CustomUserDetails(
                user.getId(),
                user.getFirebaseUid(),
                user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                user.getRole(),
                user.getStatus(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // Password is managed by Firebase, not stored locally
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != AccountStatus.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ACTIVE || status == AccountStatus.PENDING_APPROVAL || status == AccountStatus.PENDING_ACTIVATION;
    }
}
