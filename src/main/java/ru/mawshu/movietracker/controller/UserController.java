package ru.mawshu.movietracker.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.mawshu.movietracker.domain.User;
import ru.mawshu.movietracker.dto.CreateUserRequest;
import ru.mawshu.movietracker.dto.UserResponse;
import ru.mawshu.movietracker.repository.UserRepository;
import ru.mawshu.movietracker.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return toResponse(user);
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserController::toResponse)
                .toList();
    }

    private static UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getUsername(),
                u.getCreatedAt()
        );
    }
}
