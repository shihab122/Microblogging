package com.codewithshihab.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review implements Serializable {
    private VoteType voteType;
    private User reviewBy;
    private LocalDateTime reviewOn;
    private List<Comment> commentList;
}
