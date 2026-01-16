package ru.mawshu.movietracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import ru.mawshu.movietracker.TestMocksConfig;
import ru.mawshu.movietracker.domain.User;
import ru.mawshu.movietracker.dto.CreateUserRequest;
import ru.mawshu.movietracker.repository.UserRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestMocksConfig.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UserRepository userRepository; // Mockito mock из TestMocksConfig

    @Test
    void createUser_success_returns200() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("test@mail.ru");
        req.setUsername("testuser");
        req.setPassword("pass");

        when(userRepository.existsByEmail("test@mail.ru")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        User saved = new User();
        ReflectionTestUtils.setField(saved, "id", 1L);
        saved.setEmail("test@mail.ru");
        saved.setUsername("testuser");
        saved.setPassword("pass");
        saved.setCreatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@mail.ru"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createUser_invalidEmail_returns400() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("bad");
        req.setUsername("u");
        req.setPassword("p");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_emailExists_returns409_ApiError() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("test@mail.ru");
        req.setUsername("testuser");
        req.setPassword("pass");

        when(userRepository.existsByEmail("test@mail.ru")).thenReturn(true);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email already exists"))
                .andExpect(jsonPath("$.path").value("/api/users"));
    }
}
