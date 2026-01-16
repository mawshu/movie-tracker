package ru.mawshu.movietracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mawshu.movietracker.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

}
