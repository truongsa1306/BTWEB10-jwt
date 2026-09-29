package vn.iotstar.btweb10.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserModel {
    @NotBlank(message = "email is required")
    @Email(message = "email is invalid")
    @Size(max = 100, message = "email must be at most 100 characters")
    private String email;

    // BCrypt only uses the first 72 bytes
    @NotBlank(message = "password is required")
    @Size(min = 6, max = 72, message = "password must be 6-72 characters")
    private String password;

    @NotBlank(message = "fullName is required")
    @Size(max = 100, message = "fullName must be at most 100 characters")
    private String fullName;

    @Size(max = 500, message = "images must be at most 500 characters")
    private String images;
}
