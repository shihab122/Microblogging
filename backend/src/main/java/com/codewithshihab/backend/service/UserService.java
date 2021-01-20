package com.codewithshihab.backend.service;

import com.codewithshihab.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class UserService  implements Serializable {
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${jwt.header}")
    private String jwtHeader;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.active.duration}")
    private int jwtActiveDuration;

    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    @Bean
    PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }

    public UserService(MongoTemplate mongoTemplate, UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.mongoTemplate = mongoTemplate;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


}
