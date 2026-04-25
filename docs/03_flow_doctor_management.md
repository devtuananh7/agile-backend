# Luồng 3: Quản lý Bác sĩ (Admin)

> Mô tả chi tiết luồng tạo tài khoản bác sĩ, cập nhật profile chuyên môn, và phê duyệt bác sĩ.

---

## 3.1 Tổng quan

Bác sĩ **KHÔNG THỂ** tự đăng ký. Quy trình:

```
Admin tạo account → Doctor đăng nhập + cập nhật profile → Admin phê duyệt → Doctor ACTIVE
```

**Trạng thái lifecycle của Doctor:**

```
PENDING_ACTIVATION ──(Doctor login + update profile)──▶ PENDING_APPROVAL ──(Admin approve)──▶ ACTIVE
```

---

## 3.2 Sequence Diagram — Admin tạo tài khoản Bác sĩ

```
┌────────┐  ┌──────────────────┐  ┌──────────────┐  ┌──────────┐  ┌────────────┐
│ Admin  │  │AdminUserController│  │ UserService  │  │ Firebase │  │ PostgreSQL │
│ Client │  │                  │  │              │  │   Auth   │  │            │
└───┬────┘  └────────┬─────────┘  └──────┬───────┘  └────┬─────┘  └──────┬─────┘
    │               │                   │                │               │
    │ POST /api/v1/admin/users/doctor   │                │               │
    │ Authorization: Bearer <admin-token>                │               │
    │ Body: {email, phoneNumber,        │                │               │
    │        initialPassword}           │                │               │
    │──────────────▶│                   │                │               │
    │               │                   │                │               │
    │               │ adminCreateDoctor(request)         │               │
    │               │──────────────────▶│                │               │
    │               │                   │                │               │
    │               │                   │ ① Check email  │               │
    │               │                   │    uniqueness  │               │
    │               │                   │───────────────────────────────▶│
    │               │                   │                │               │
    │               │                   │ ② Create Firebase user        │
    │               │                   │   (email + initialPassword)   │
    │               │                   │───────────────▶│               │
    │               │                   │                │               │
    │               │                   │   FirebaseUser │               │
    │               │                   │   (uid)        │               │
    │               │                   │◀───────────────│               │
    │               │                   │                │               │
    │               │                   │ ③ Set custom claims            │
    │               │                   │   {role: "DOCTOR"}            │
    │               │                   │───────────────▶│               │
    │               │                   │                │               │
    │               │                   │ ④ INSERT User in PG           │
    │               │                   │   firebaseUid = uid           │
    │               │                   │   role = DOCTOR               │
    │               │                   │   status = PENDING_ACTIVATION │
    │               │                   │───────────────────────────────▶│
    │               │                   │                │               │
    │ 201 Created: User entity          │                │               │
    │◀──────────────│                   │                │               │
```

### Chi tiết code — Step ②③④

**File:** `service/UserService.java` → `adminCreateDoctor()`

```java
public User adminCreateDoctor(DoctorCreateRequestDto request) {
    // ① Check email uniqueness trong local DB
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("Email is already in use.");
    }

    // ② Create user trên Firebase Auth
    UserRecord.CreateRequest firebaseRequest = new UserRecord.CreateRequest()
            .setEmail(request.getEmail())
            .setPassword(request.getInitialPassword())
            .setEmailVerified(false);
    UserRecord firebaseUser = FirebaseAuth.getInstance().createUser(firebaseRequest);

    // ③ Set custom claims cho role
    FirebaseAuth.getInstance().setCustomUserClaims(
            firebaseUser.getUid(),
            Map.of("role", "DOCTOR"));

    // ④ Create User entity trong PostgreSQL
    User user = User.builder()
            .firebaseUid(firebaseUser.getUid())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .role(Role.DOCTOR)
            .status(AccountStatus.PENDING_ACTIVATION)
            .authProvider("password")
            .build();
    return userRepository.save(user);
}
```

**Lưu ý quan trọng:**
- Admin tạo account **TRƯỚC**, Doctor login **SAU**
- Vì account đã tồn tại trong PG → `UserSyncService.syncUser()` sẽ tìm thấy và return (KHÔNG tạo mới)
- Nếu Doctor login trước khi Admin tạo → sẽ bị auto-create thành PATIENT

---

## 3.3 Sequence Diagram — Doctor cập nhật Profile

```
┌────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────────┐
│ Doctor │  │UserController │  │ UserService  │  │ PostgreSQL │
│ Client │  │              │  │              │  │            │
└───┬────┘  └──────┬───────┘  └──────┬───────┘  └──────┬─────┘
    │              │                 │                  │
    │ PUT /api/v1/users/doctor/profile                  │
    │ Authorization: Bearer <doctor-token>              │
    │ Body: {metadata: {                                │
    │   fullName, specialization,                       │
    │   hospital, licenseNumber,                        │
    │   yearsOfExperience, bio                          │
    │ }}                                                │
    │─────────────▶│                 │                  │
    │              │                 │                  │
    │              │ doctorUpdateProfile(id, request)   │
    │              │────────────────▶│                  │
    │              │                 │                  │
    │              │                 │ ① findById(doctorId)
    │              │                 │─────────────────▶│
    │              │                 │                  │
    │              │                 │ ② Validate role == DOCTOR
    │              │                 │                  │
    │              │                 │ ③ Update metadata (JSONB)
    │              │                 │                  │
    │              │                 │ ④ Set status =   │
    │              │                 │   PENDING_APPROVAL
    │              │                 │─────────────────▶│
    │              │                 │                  │
    │ 200 OK: Updated User          │                  │
    │◀─────────────│                 │                  │
```

### Chi tiết code

**File:** `service/UserService.java` → `doctorUpdateProfile()`

```java
public User doctorUpdateProfile(UUID doctorId, DoctorProfileUpdateRequestDto request) {
    User user = userRepository.findById(doctorId)
            .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

    // Validate role
    if (user.getRole() != Role.DOCTOR) {
        throw new IllegalArgumentException("User is not a doctor");
    }

    // Update metadata
    if (request.getMetadata() != null) {
        user.setMetadata(request.getMetadata());
    }

    // Chuyển sang PENDING_APPROVAL để Admin review
    user.setStatus(AccountStatus.PENDING_APPROVAL);

    return userRepository.save(user);
}
```

**DoctorMetadataDto** chứa:

| Field | Type | Mô tả |
|-------|------|--------|
| `fullName` | String | Họ tên đầy đủ |
| `specialization` | String | Chuyên khoa (Tim mạch, Nội khoa, ...) |
| `hospital` | String | Bệnh viện/cơ sở công tác |
| `licenseNumber` | String | Số giấy phép hành nghề |
| `yearsOfExperience` | Integer | Số năm kinh nghiệm |
| `bio` | String | Giới thiệu bản thân |

---

## 3.4 Sequence Diagram — Admin phê duyệt Bác sĩ

```
┌────────┐  ┌──────────────────┐  ┌──────────────┐  ┌──────────┐  ┌────────────┐
│ Admin  │  │AdminUserController│  │ UserService  │  │ Firebase │  │ PostgreSQL │
└───┬────┘  └────────┬─────────┘  └──────┬───────┘  └────┬─────┘  └──────┬─────┘
    │               │                   │                │               │
    │ PATCH /api/v1/admin/users/{id}/approve             │               │
    │ Authorization: Bearer <admin-token>                │               │
    │──────────────▶│                   │                │               │
    │               │                   │                │               │
    │               │ adminApproveDoctor(doctorId)       │               │
    │               │──────────────────▶│                │               │
    │               │                   │                │               │
    │               │                   │ ① findById + validate DOCTOR  │
    │               │                   │───────────────────────────────▶│
    │               │                   │                │               │
    │               │                   │ ② Set status = ACTIVE         │
    │               │                   │───────────────────────────────▶│
    │               │                   │                │               │
    │               │                   │ ③ Update Firebase claims      │
    │               │                   │   {role:"DOCTOR", approved:true}
    │               │                   │───────────────▶│               │
    │               │                   │                │               │
    │ 200 OK: User (status=ACTIVE)      │                │               │
    │◀──────────────│                   │                │               │
```

### Chi tiết code

**File:** `service/UserService.java` → `adminApproveDoctor()`

```java
public User adminApproveDoctor(UUID doctorId) {
    User user = userRepository.findById(doctorId)
            .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

    if (user.getRole() != Role.DOCTOR) {
        throw new IllegalArgumentException("User is not a doctor");
    }

    // ② Activate trong PG
    user.setStatus(AccountStatus.ACTIVE);

    // ③ Update Firebase custom claims
    try {
        FirebaseAuth.getInstance().setCustomUserClaims(
                user.getFirebaseUid(),
                Map.of("role", "DOCTOR", "approved", true));
    } catch (FirebaseAuthException e) {
        log.warn("Failed to update Firebase claims, PG is source of truth");
    }

    return userRepository.save(user);
}
```

**Lưu ý:** PostgreSQL là **source of truth** cho role/status. Nếu Firebase claims update fail, hệ thống vẫn hoạt động đúng.

---

## 3.5 Tổng hợp APIs

| Method | Path | Auth | Mô tả |
|--------|------|------|--------|
| `POST` | `/api/v1/admin/users/doctor` | ADMIN | Tạo tài khoản bác sĩ |
| `PATCH` | `/api/v1/admin/users/{id}/approve` | ADMIN | Phê duyệt bác sĩ |
| `PUT` | `/api/v1/users/doctor/profile` | DOCTOR | Doctor cập nhật profile |

---

*File: 03_flow_doctor_management.md | Project: CareTalk Backend*
