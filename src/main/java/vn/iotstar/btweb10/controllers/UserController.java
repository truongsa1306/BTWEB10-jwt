package vn.iotstar.btweb10.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.iotstar.btweb10.entity.User;
import vn.iotstar.btweb10.models.UserResponse;
import vn.iotstar.btweb10.services.UserService;

import java.util.List;

@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            throw new InsufficientAuthenticationException("Authentication is required");
        }
        return ResponseEntity.ok(UserResponse.from(currentUser));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> allUsers() {
        return ResponseEntity.ok(userService.allUsers().stream().map(UserResponse::from).toList());
    }
}
