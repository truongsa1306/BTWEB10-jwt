package vn.iotstar.btweb10.services;

import org.springframework.stereotype.Service;
import vn.iotstar.btweb10.entity.User;
import vn.iotstar.btweb10.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {
        return userRepository.findAll();
    }
}
