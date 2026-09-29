package vn.iotstar.btweb10.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.btweb10.entity.User;
import vn.iotstar.btweb10.exception.EmailAlreadyExistsException;
import vn.iotstar.btweb10.models.LoginUserModel;
import vn.iotstar.btweb10.models.RegisterUserModel;
import vn.iotstar.btweb10.repository.UserRepository;

import java.util.Locale;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository,
                                 AuthenticationManager authenticationManager,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User signup(RegisterUserModel input) {
        String email = normalize(input.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = new User();
        user.setFullName(input.getFullName().trim());
        user.setEmail(email);
        user.setImages(input.getImages());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setRole(User.ROLE_USER);
        return userRepository.save(user);
    }

    public User authenticate(LoginUserModel input) {
        String email = normalize(input.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, input.getPassword()));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
