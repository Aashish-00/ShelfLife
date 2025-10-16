package com.shelflife.shelflife.service;

import com.shelflife.shelflife.model.User;
import com.shelflife.shelflife.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public boolean emailExists(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElse(null);
    }
}
