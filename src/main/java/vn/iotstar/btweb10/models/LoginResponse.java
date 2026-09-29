package vn.iotstar.btweb10.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    /** Token lifetime in milliseconds. */
    private long expiresIn;
}
