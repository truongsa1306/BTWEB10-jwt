package vn.iotstar.btweb10.models;

import vn.iotstar.btweb10.entity.User;

import java.time.Instant;

/** Public view of a user. Deliberately has no password / account-flag fields. */
public record UserResponse(
        Integer id,
        String fullName,
        String email,
        String images,
        String role,
        Instant createdAt,
        Instant updatedAt) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getImages(),
                u.getRole(), u.getCreatedAt(), u.getUpdatedAt());
    }
}
