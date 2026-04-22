package vn.hust.agilechatbotbackend.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.agilechatbotbackend.dto.DoctorCreateRequestDto;
import vn.hust.agilechatbotbackend.dto.DoctorProfileUpdateRequestDto;
import vn.hust.agilechatbotbackend.entity.User;
import vn.hust.agilechatbotbackend.enums.Role;
import vn.hust.agilechatbotbackend.enums.AccountStatus;
import vn.hust.agilechatbotbackend.repository.UserRepository;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Admin creates a doctor account.
     * 1. Creates user on Firebase Auth (via Admin SDK)
     * 2. Sets custom claims for role
     * 3. Creates User entity in PostgreSQL
     */
    @Transactional
    public User adminCreateDoctor(DoctorCreateRequestDto request) {
        // Check email uniqueness in local DB
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        try {
            // 1. Create user on Firebase
            UserRecord.CreateRequest firebaseRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getInitialPassword())
                    .setEmailVerified(false);

            UserRecord firebaseUser = FirebaseAuth.getInstance().createUser(firebaseRequest);
            log.info("Created Firebase user for doctor: uid={}, email={}", firebaseUser.getUid(), request.getEmail());

            // 2. Set custom claims
            FirebaseAuth.getInstance().setCustomUserClaims(
                    firebaseUser.getUid(),
                    Map.of("role", "DOCTOR"));

            // 3. Create User entity in PostgreSQL
            User user = User.builder()
                    .firebaseUid(firebaseUser.getUid())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .role(Role.DOCTOR)
                    .status(AccountStatus.PENDING_ACTIVATION)
                    .authProvider("password")
                    .build();

            return userRepository.save(user);

        } catch (FirebaseAuthException e) {
            log.error("Failed to create Firebase user for doctor: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to create doctor account: " + e.getMessage());
        }
    }

    @Transactional
    public User doctorUpdateProfile(UUID doctorId, DoctorProfileUpdateRequestDto request) {
        User user = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (user.getRole() != Role.DOCTOR) {
            throw new IllegalArgumentException("User is not a doctor");
        }

        if (request.getMetadata() != null) {
            user.setMetadata(request.getMetadata());
        }

        user.setStatus(AccountStatus.PENDING_APPROVAL);

        return userRepository.save(user);
    }

    @Transactional
    public User adminApproveDoctor(UUID doctorId) {
        User user = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (user.getRole() != Role.DOCTOR) {
            throw new IllegalArgumentException("User is not a doctor");
        }

        user.setStatus(AccountStatus.ACTIVE);

        // Update Firebase custom claims to include approved flag
        try {
            if (user.getFirebaseUid() != null) {
                FirebaseAuth.getInstance().setCustomUserClaims(
                        user.getFirebaseUid(),
                        Map.of("role", "DOCTOR", "approved", true));
            }
        } catch (FirebaseAuthException e) {
            log.warn("Failed to update Firebase claims for doctor {}: {}", doctorId, e.getMessage());
            // Continue — PG is source of truth for role/status
        }

        return userRepository.save(user);
    }
}
