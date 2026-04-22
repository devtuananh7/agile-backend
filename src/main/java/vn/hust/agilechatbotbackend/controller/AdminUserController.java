package vn.hust.agilechatbotbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hust.agilechatbotbackend.dto.DoctorCreateRequestDto;
import vn.hust.agilechatbotbackend.entity.User;
import vn.hust.agilechatbotbackend.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/doctor")
    public ResponseEntity<?> createDoctor(@RequestBody DoctorCreateRequestDto request) {
        try {
            User user = userService.adminCreateDoctor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<User> approveDoctor(@PathVariable("id") UUID doctorId) {
        User user = userService.adminApproveDoctor(doctorId);
        return ResponseEntity.ok(user);
    }
}
