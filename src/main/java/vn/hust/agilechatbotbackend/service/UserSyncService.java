package vn.hust.agilechatbotbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.agilechatbotbackend.entity.User;
import vn.hust.agilechatbotbackend.enums.AccountStatus;
import vn.hust.agilechatbotbackend.enums.Role;
import vn.hust.agilechatbotbackend.repository.UserRepository;

import java.util.Optional;

/**
 * Synchronizes Firebase users with the local PostgreSQL users table.
 * On first authentication, auto-creates a User with role PATIENT.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository userRepository;

    /**
     * Find or create a User based on Firebase UID.
     * Called by FirebaseAuthFilter on every authenticated request.
     */
    @Transactional
    public User syncUser(String firebaseUid, String email, String displayName, String authProvider) {
        Optional<User> existing = userRepository.findByFirebaseUid(firebaseUid);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Auto-create patient on first login
        User newUser = User.builder()
                .firebaseUid(firebaseUid)
                .email(email)
                .role(Role.PATIENT)
                .status(AccountStatus.ACTIVE)
                .authProvider(authProvider)
                .build();

        User saved = userRepository.save(newUser);
        log.info("Auto-created user for Firebase uid={}, email={}, provider={}", firebaseUid, email, authProvider);
        return saved;
    }

    /**
     * Update user profile data (phone, metadata) after registration.
     */
    @Transactional
    public User updateProfile(String firebaseUid, String phoneNumber,
                              vn.hust.agilechatbotbackend.dto.metadata.UserMetadata metadata) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("User not found for uid: " + firebaseUid));

        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }
        if (metadata != null) {
            user.setMetadata(metadata);
        }

        return userRepository.save(user);
    }
}
