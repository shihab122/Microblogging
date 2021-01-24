package com.codewithshihab.backend.controller;

import com.codewithshihab.backend.exception.ExecutionFailureException;
import com.codewithshihab.backend.models.LoginRequestBody;
import com.codewithshihab.backend.models.SendToken;
import com.codewithshihab.backend.models.User;
import com.codewithshihab.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "*", exposedHeaders = {"httpStatus", "messageType", "messageTitle", "messageDescription", "servedAt"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RequestMapping(value = "/user/", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("get-user-from-token/{accessToken}")
    public ResponseEntity<?> getUserFromAccessToken(@PathVariable String accessToken, HttpServletRequest httpServletRequest) {
        try {
            return new ResponseEntity<>(userService.getUserFromAccessToken(accessToken), HttpStatus.OK);
        } catch (ExecutionFailureException e) {
            return new ResponseEntity<>(e.getError(), HttpStatus.valueOf(e.getError().getCode()));
        }
    }

    @PostMapping("create")
    public ResponseEntity<?> createUser(@RequestBody User user, HttpServletRequest httpServletRequest) {
        try {
            return new ResponseEntity<>(userService.save(user), HttpStatus.CREATED);
        } catch (ExecutionFailureException e) {
            return new ResponseEntity<>(e.getError(), HttpStatus.valueOf(e.getError().getCode()));
        }
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginRequestBody loginRequestBody, HttpServletRequest httpServletRequest) {
        try {
            return new ResponseEntity<>(new SendToken(userService.login(loginRequestBody)), HttpStatus.OK);
        } catch (ExecutionFailureException e) {
            return new ResponseEntity<>(e.getError(), HttpStatus.valueOf(e.getError().getCode()));
        }
    }
}
