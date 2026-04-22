package vn.hust.agilechatbotbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hust.agilechatbotbackend.entity.User;
import vn.hust.agilechatbotbackend.enums.AccountStatus;
import vn.hust.agilechatbotbackend.enums.Role;
import vn.hust.agilechatbotbackend.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSyncServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSyncService userSyncService;

    @Test
    void shouldCreateNewUserOnFirstSync() {
        // Given
        String firebaseUid = "new-uid-123";
        String email = "newuser@example.com";
        String name = "New User";
        String provider = "google.com";

        when(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // When
        User result = userSyncService.syncUser(firebaseUid, email, name, provider);

        // Then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getFirebaseUid()).isEqualTo(firebaseUid);
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getRole()).isEqualTo(Role.PATIENT);
        assertThat(saved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getAuthProvider()).isEqualTo("google.com");
        assertThat(saved.getPasswordHash()).isNull();

        assertThat(result).isNotNull();
        assertThat(result.getFirebaseUid()).isEqualTo(firebaseUid);
    }

    @Test
    void shouldReturnExistingUserOnSubsequentSync() {
        // Given
        String firebaseUid = "existing-uid-456";
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .firebaseUid(firebaseUid)
                .email("existing@example.com")
                .role(Role.PATIENT)
                .status(AccountStatus.ACTIVE)
                .build();

        when(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(Optional.of(existingUser));

        // When
        User result = userSyncService.syncUser(firebaseUid, "existing@example.com", "Existing User", "password");

        // Then
        verify(userRepository, never()).save(any());
        assertThat(result).isSameAs(existingUser);
    }

    @Test
    void shouldCreateDoctorAsPatientOnAutoSync() {
        // Even if someone signs in who will become a doctor,
        // auto-sync always creates as PATIENT. Admin must upgrade role.
        String firebaseUid = "future-doctor-uid";

        when(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userSyncService.syncUser(firebaseUid, "doctor@hospital.com", "Dr. Test", "password");

        assertThat(result.getRole()).isEqualTo(Role.PATIENT);
        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }
}
