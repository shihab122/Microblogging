package com.codewithshihab.backend.service;

import com.codewithshihab.backend.exception.ExecutionFailureException;
import com.codewithshihab.backend.models.Error;
import com.codewithshihab.backend.models.*;
import com.codewithshihab.backend.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class PostService implements Serializable {
    private PostRepository postRepository;
    private UserService userService;

    public PostService(PostRepository postRepository, UserService userService) {
        this.userService = userService;
        this.postRepository = postRepository;
    }

    public Post create(Post post, String accessToken) throws ExecutionFailureException {
        validatePost(post);
        post.setPostedAt(LocalDateTime.now());
        post.setPostedBy(userService.getUserFromAccessToken(accessToken));
        post.setVoteList(new ArrayList<>());
        post.setCommentList(new ArrayList<>());

        ActivityFeed activityFeed = new ActivityFeed();
        post.setActivityFeedList(new ArrayList<>());
        activityFeed.setTitle("Post was created");
        activityFeed.setDescription("");
        activityFeed.setActionOn(LocalDateTime.now());
        post.getActivityFeedList().add(activityFeed);
        return postRepository.insert(post);
    }

    public Post addVote(String postId, Vote vote, String accessToken) throws ExecutionFailureException {
        vote.setVoteBy(userService.getUserFromAccessToken(accessToken));
        validateVote(vote);
        vote.setVoteOn(LocalDateTime.now());

        Optional<Post> optionalPost = postRepository.findById(postId);
        if (!optionalPost.isPresent()) {
            throw new ExecutionFailureException(
                new Error(400, "id", "Post id does not exist", "Post id was not found in the database")
            );
        }

        optionalPost.get().getVoteList().add(vote);

        ActivityFeed activityFeed = new ActivityFeed();
        activityFeed.setTitle("Vote was added");
        activityFeed.setDescription("");
        activityFeed.setActionOn(LocalDateTime.now());
        optionalPost.get().getActivityFeedList().add(activityFeed);
        return postRepository.save(optionalPost.get());
    }

    public Post addComment(String postId, Comment comment, String accessToken) throws ExecutionFailureException {
        comment.setCommentBy(userService.getUserFromAccessToken(accessToken));
        validateComment(comment);
        comment.setCommentOn(LocalDateTime.now());

        Optional<Post> optionalPost = postRepository.findById(postId);
        if (!optionalPost.isPresent()) {
            throw new ExecutionFailureException(
                    new Error(400, "id", "Post id does not exist", "Post id was not found in the database")
            );
        }

        optionalPost.get().getCommentList().add(comment);

        ActivityFeed activityFeed = new ActivityFeed();
        activityFeed.setTitle("Comment was added");
        activityFeed.setDescription("");
        activityFeed.setActionOn(LocalDateTime.now());
        optionalPost.get().getActivityFeedList().add(activityFeed);
        return postRepository.save(optionalPost.get());
    }

    private void validateVote(Vote vote) throws ExecutionFailureException {
        if (vote == null) {
            throw new ExecutionFailureException(
                new Error(400, "voteType", "Invalid Vote", "Vote can not be null.")
            );
        }
        else if (vote.getVoteType() == null) {
            throw new ExecutionFailureException(
                    new Error(400, "voteType", "Invalid Vote Type", "Vote type can not be empty.")
            );
        }
        else if (vote.getVoteBy() == null) {
            throw new ExecutionFailureException(
                    new Error(400, "user", "Invalid User", "Only valid user can vote.")
            );
        }
    }

    private void validateComment(Comment comment) throws ExecutionFailureException {
        if (comment == null) {
            throw new ExecutionFailureException(
                    new Error(400, "comment", "Invalid Comment", "Comment can not be null.")
            );
        }
        else if (comment.getCommentBy() == null) {
            throw new ExecutionFailureException(
                    new Error(400, "user", "Invalid User", "Only valid user can comment.")
            );
        }
        else if (comment.getDescription() == null || comment.getDescription().length() == 0) {
            throw new ExecutionFailureException(
                    new Error(400, "description", "Invalid Comment", "Comment must have at least one character.")
            );
        }
    }

    private void validatePost(Post post) throws ExecutionFailureException {
        if (post.getDescription() == null || post.getDescription().length() <= 2) {
            throw new ExecutionFailureException(
                new Error(400, "description", "Invalid Description", "Description should be more than 2 characters.")
            );
        }
        else if (post.getPostedBy() == null || post.getPostedBy().getUsername() == null) {
            throw new ExecutionFailureException(
                new Error(400, "username", "Invalid User", "Only valid user can create a post.")
            );
        }
    }
}