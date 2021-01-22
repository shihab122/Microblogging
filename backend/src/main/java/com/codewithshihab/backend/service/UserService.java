package com.codewithshihab.backend.service;

import com.codewithshihab.backend.exception.ExecutionFailureException;
import com.codewithshihab.backend.models.ActivityFeed;
import com.codewithshihab.backend.models.Error;
import com.codewithshihab.backend.models.LoginRequestBody;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        if (username.isEmpty()) {
            throw new ExecutionFailureException(
                    new Error(400, "username", "Username is empty", "Username is required field")
            );
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new ExecutionFailureException(
                    new Error(400, "username", "Username length is invalid", "Username can be more than 2 & less than 50 characters")
            );
        }
        if (!username.matches("^[a-z1-9._@]+$")) {
            throw new ExecutionFailureException(
                    new Error(400, "username", "Username has invalid character", "Username can have only alphabets")
            );
        }
    }

    private void validatePassword(String password) throws ExecutionFailureException {
        if (password == null || password.equals("")) {
            throw new ExecutionFailureException(
                    new Error(400, "password", "Invalid password", "Password is empty")
            );
        }
        if (password.length() < 6) {
            throw new ExecutionFailureException(
                    new Error(400, "password", "Invalid password length", "Password is empty")
            );
        }
    }

    private void validateUser(User user) throws ExecutionFailureException {
        validateUsername(Optional.ofNullable(user.getUsername()).orElse(""));
        validatePassword(Optional.ofNullable(user.getPassword()).orElse(""));

        if (user.getName() == null
                || (user.getName().getFirstName().isEmpty()
                && user.getName().getMiddleName().isEmpty()
                && user.getName().getLastName().isEmpty())
        ) {
            throw new ExecutionFailureException(
                    new Error(400, "name", "Invalid Name", "Name must be 2 characters long.")
            );
        }
    }

    public User save(User user) throws ExecutionFailureException {
//      Validate object
        validateUser(user);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        if (optionalUser.isPresent())
            throw new ExecutionFailureException(
                    new Error(400, "username", "Username already exist", "Please choose another username.")
            );

        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword().trim()));
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setActivityFeedList(new ArrayList<>());

        ActivityFeed activityFeed = new ActivityFeed();
        activityFeed.setTitle("User was created");
        activityFeed.setDescription("");
        activityFeed.setActionOn(LocalDateTime.now());
        newUser.getActivityFeedList().add(activityFeed);

        return userRepository.insert(newUser);
    }

    public User getByUsername(String username) throws ExecutionFailureException {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (!optionalUser.isPresent()) {
            throw new ExecutionFailureException(new Error(400, "username", "Invalid Username", "Username does not exist."));
        }
        return optionalUser.get();
    }

    public Page<User> findAll(Integer size, Integer offset) {
        final Integer pageSize = Optional.ofNullable(size).orElse(DEFAULT_PAGE_SIZE);
        final Integer pageOffset = Optional.ofNullable(offset).orElse(DEFAULT_PAGE_OFFSET);
        final PageRequest pageRequest = PageRequest.of(pageOffset, pageSize);
        return userRepository.findAll(pageRequest);
    }

    public String login(LoginRequestBody loginRequestBody) throws ExecutionFailureException {
        validateUsername(Optional.ofNullable(loginRequestBody.getUsername()).orElse(""));
        validatePassword(Optional.ofNullable(loginRequestBody.getPassword()).orElse(""));

        Optional<User> optionalUser = userRepository.findByUsername(loginRequestBody.getUsername());
        if (!optionalUser.isPresent()) {
            throw new ExecutionFailureException(new Error(400, "username", "Invalid User", "Username is invalid."));
        }

        ActivityFeed activityFeed = new ActivityFeed();
        activityFeed.setDescription("");
        activityFeed.setActionOn(LocalDateTime.now());

        User user = optionalUser.get();

        // Invalid Password
        if (!passwordEncoder.matches(loginRequestBody.getPassword(), user.getPassword())) {
            activityFeed.setTitle("Login attempt failed");

            Query query = Query.query(Criteria.where("id").is(user.getId()));
            Update update = new Update();
            update.push("activityFeedList", activityFeed);
            mongoTemplate.updateFirst(query, update, User.class);
            throw new ExecutionFailureException(new Error(401, "password", "Invalid login", "Password is invalid."));
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
                .setExpiration(generateExpirationDate(loginRequestBody.isRememberMe()))
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

    public User getUserFromAccessToken(String accessToken) throws ExecutionFailureException {
        return getByUsername(getUsernameFromAccessToken(accessToken));
    }

    public String getUsernameFromAccessToken(String accessToken) {
        String username;
        try {
            final Claims claims = getClaimsFromAccessToken(accessToken);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    private Claims getClaimsFromAccessToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

}
