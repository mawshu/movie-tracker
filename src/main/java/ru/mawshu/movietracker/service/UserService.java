package ru.mawshu.movietracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mawshu.movietracker.dto.CreateUserRequest;
import ru.mawshu.movietracker.domain.User;
import ru.mawshu.movietracker.repository.UserRepository;
import ru.mawshu.movietracker.exception.ConflictException;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        validateUserUniqueness(request.getEmail(), request.getUsername());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());

        return userRepository.save(user);
    }

    public void validateUserUniqueness(String email, String username) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }
    }
}
