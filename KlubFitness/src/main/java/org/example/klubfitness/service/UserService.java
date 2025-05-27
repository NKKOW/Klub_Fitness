package org.example.klubfitness.service;

import org.example.klubfitness.entity.User;
import org.example.klubfitness.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public User createUser(User user) {
        return repo.save(user);
    }

    public User getUserById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public User updateUser(Long id, User payload) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setUsername(payload.getUsername());
                    existing.setPassword(payload.getPassword());
                    existing.setRole(payload.getRole());
                    return repo.save(existing);
                })
                .orElse(null);
    }

    public boolean deleteUser(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}