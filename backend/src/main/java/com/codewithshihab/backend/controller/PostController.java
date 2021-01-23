package com.codewithshihab.backend.controller;

import com.codewithshihab.backend.exception.ExecutionFailureException;
import com.codewithshihab.backend.models.Comment;
import com.codewithshihab.backend.models.Post;
import com.codewithshihab.backend.models.Vote;
import com.codewithshihab.backend.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "*", exposedHeaders = {"httpStatus", "messageType", "messageTitle", "messageDescription", "servedAt"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RequestMapping(value = "/post/", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("list")
    public ResponseEntity<?> findAll(HttpServletRequest httpServletRequest) {
        return new ResponseEntity<>(postService.findAll(), HttpStatus.OK);
    }

    @PostMapping("view/{postId}")
    public ResponseEntity<?> findPostById(@PathVariable String postId, HttpServletRequest httpServletRequest) {
        try {
            return new ResponseEntity<>(postService.findPostById(postId), HttpStatus.OK);
        } catch (ExecutionFailureException e) {
            return new ResponseEntity<>(e.getError(), HttpStatus.valueOf(e.getError().getCode()));
        }
    }

    @PostMapping("create/{accessToken}")
    public ResponseEntity<?> create(@PathVariable String accessToken, @RequestBody Post post, HttpServletRequest httpServletRequest) {
        try {
            return new ResponseEntity<>(postService.create(post, accessToken), HttpStatus.CREATED);
        } catch (ExecutionFailureException e) {
            return new ResponseEntity<>(e.getError(), HttpStatus.valueOf(e.getError().getCode()));
        }
    }

    @PostMapping("add-vote/{postId}/{accessToken}")
    public ResponseEntity<?> addComment(@PathVariable String postId, @PathVariable String accessToken, @RequestBody Vote vote, HttpServletRequest httpServletRequest) {
        try {
            return new ResponseEntity<>(postService.addVote(vote, postId, accessToken), HttpStatus.CREATED);
        } catch (ExecutionFailureException e) {
            return new ResponseEntity<>(e.getError(), HttpStatus.valueOf(e.getError().getCode()));
        }
    }

    @PostMapping("add-comment/{postId}/{accessToken}")
    public ResponseEntity<?> addComment(@PathVariable String postId, @PathVariable String accessToken, @RequestBody Comment comment, HttpServletRequest httpServletRequest) {
        try {
            return new ResponseEntity<>(postService.addComment(comment, postId, accessToken), HttpStatus.CREATED);
        } catch (ExecutionFailureException e) {
            return new ResponseEntity<>(e.getError(), HttpStatus.valueOf(e.getError().getCode()));
        }
    }
    
}
