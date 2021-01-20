package com.codewithshihab.backend.service;

import com.codewithshihab.backend.exception.ExecutionFailureException;
import com.codewithshihab.backend.models.ActivityFeed;
import com.codewithshihab.backend.models.Error;
import com.codewithshihab.backend.models.User;
import com.codewithshihab.backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@Service
public class UserService  implements Serializable {
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final int DEFAULT_PAGE_SIZE = 10;
    private final int DEFAULT_PAGE_OFFSET = 10;

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

    private void validateUsername(String username) throws ExecutionFailureException {
        Error error = null;

        // Username
        if (StringUtils.isEmpty(username)) {
            error = new Error("400", "username", "Username is empty", "Username is required field");
        }
        if (username.length() < 3 || username.length() > 50) {
            error = new Error("400", "username", "Username length is invalid", "Username can be more than 2 & less than 50 characters");
        }
        if (!username.matches("^[a-z1-9._]+$")) {
            error = new Error("400", "username", "Username has invalid character", "Username can have only alphabets");
        }

        if (error != null) {
            throw new ExecutionFailureException(error);
        }
    }

    private void validatePassword(String password) throws ExecutionFailureException {
        Error error = null;
        if (password == null || password.equals("")) {
            error  = new Error("400", "password", "Invalid password", "Password is empty");
            throw new ExecutionFailureException(error);
        }
        if (password.length() < 6) {
            error  = new Error("400", "password", "Invalid password length", "Password is empty");
            throw new ExecutionFailureException(error);
        }
    }

    public User save(User user, ActivityFeed activityFeed) throws ExecutionFailureException {
//      Validate object
        validateUsername(Optional.ofNullable(user.getUsername()).orElse(""));
        validatePassword(Optional.ofNullable(user.getPassword()).orElse(""));

        Error error = null;
        Optional<User> optionalUser = getByUsername(user.getUsername());
        if (optionalUser.isPresent())
            error = new Error("400", "username", "Username already exist", "Please choose another username.");

        if (error != null) throw new ExecutionFailureException(error);

        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword().trim()));
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setActivityFeedList(new ArrayList<>());

        activityFeed.setTitle("User was created");
        activityFeed.setDescription("");
        activityFeed.setActionOn(LocalDateTime.now());
        newUser.getActivityFeedList().add(activityFeed);

        return userRepository.insert(newUser);
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Page<User> findAll(Integer size, Integer offset) {
        final Integer pageSize = Optional.ofNullable(size).orElse(DEFAULT_PAGE_SIZE);
        final Integer pageOffset = Optional.ofNullable(offset).orElse(DEFAULT_PAGE_OFFSET);
        final PageRequest pageRequest = PageRequest.of(pageOffset, pageSize);
        return userRepository.findAll(pageRequest);
    }

    public String login(String username, String password, boolean rememberMe, ActivityFeed activityFeed) throws ExecutionFailureException {
        validateUsername(Optional.ofNullable(username).orElse(""));
        validatePassword(Optional.ofNullable(password).orElse(""));

        Optional<User> optionalUser = getByUsername(username);
        if (optionalUser.isPresent()) {
            throw new ExecutionFailureException(new Error("400", "username", "Invalid login", "Username is invalid."));
        }

        activityFeed.setDescription("");
        activityFeed.setActionOn(LocalDateTime.now());

        User user = optionalUser.get();

        // Invalid Password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            activityFeed.setTitle("Login attempt failed");

            Query query = Query.query(Criteria.where("id").is(user.getId()));
            Update update = new Update();
            update.push("activityFeedList", activityFeed);
            mongoTemplate.updateFirst(query, update, User.class);
            throw new ExecutionFailureException(new Error("400", "password", "Invalid login", "Password is invalid."));
        }

        // Valid Password
        activityFeed.setTitle("Login succeed");
        Query query = Query.query(Criteria.where("id").is(user.getId()));
        Update update = new Update();
        update.push("activityFeedList", activityFeed);
        mongoTemplate.updateFirst(query, update, User.class);

        // Generating JWT
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(applicationName)
                .setIssuedAt(generateCurrentDate())
                .setExpiration(generateExpirationDate(rememberMe))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }


    private long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    private Date generateCurrentDate() {
        return new Date(getCurrentTimeMillis());
    }


    private Date generateExpirationDate(boolean rememberMe) {
        if (rememberMe) {
            return new Date(getCurrentTimeMillis() + 10080 * 1000);
        }
        return new Date(getCurrentTimeMillis() + (jwtActiveDuration * 1000L));
    }


}
