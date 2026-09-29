package vn.iotstar.btweb10.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.iotstar.btweb10.entity.User;
import vn.iotstar.btweb10.models.LoginResponse;
import vn.iotstar.btweb10.models.LoginUserModel;
import vn.iotstar.btweb10.models.RegisterUserModel;
import vn.iotstar.btweb10.models.UserResponse;
import vn.iotstar.btweb10.services.AuthenticationService;
import vn.iotstar.btweb10.services.JwtService;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserModel registerUser) {
        User registeredUser = authenticationService.signup(registerUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginUserModel loginUser) {
        User authenticatedUser = authenticationService.authenticate(loginUser);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        return ResponseEntity.ok(new LoginResponse(jwtToken, jwtService.getExpirationTime()));
    }
}
