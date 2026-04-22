package vn.hust.agilechatbotbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.hust.agilechatbotbackend.dto.DoctorProfileUpdateRequestDto;
import vn.hust.agilechatbotbackend.dto.FirebaseRegisterRequest;
import vn.hust.agilechatbotbackend.entity.User;
import vn.hust.agilechatbotbackend.security.CustomUserDetails;
import vn.hust.agilechatbotbackend.service.UserService;
import vn.hust.agilechatbotbackend.service.UserSyncService;
import vn.hust.agilechatbotbackend.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserSyncService userSyncService;
    private final UserRepository userRepository;

    /**
     * Get the authenticated user's profile.
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userRepository.findByFirebaseUid(userDetails.getFirebaseUid())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update the authenticated user's profile (phone, metadata).
     * Email changes are ignored — managed by Firebase.
     * PUT /api/v1/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FirebaseRegisterRequest request) {

        User updated = userSyncService.updateProfile(
                userDetails.getFirebaseUid(),
                request.getPhoneNumber(),
                request.getMetadata());
        return ResponseEntity.ok(updated);
    }

    /**
     * Register a Firebase-authenticated user with extra profile data.
     * POST /api/v1/users/register-firebase
     */
    @PostMapping("/register-firebase")
    public ResponseEntity<User> registerFirebaseUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FirebaseRegisterRequest request) {

        User updated = userSyncService.updateProfile(
                userDetails.getFirebaseUid(),
                request.getPhoneNumber(),
                request.getMetadata());
        return ResponseEntity.ok(updated);
    }

    /**
     * Doctor updates their own profile.
     * PUT /api/v1/users/doctor/profile
     */
    @PutMapping("/doctor/profile")
    public ResponseEntity<User> updateDoctorProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody DoctorProfileUpdateRequestDto request) {

        User user = userService.doctorUpdateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(user);
    }
}
