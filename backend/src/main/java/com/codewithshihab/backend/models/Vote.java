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
public class Vote implements Serializable {
    private VoteType voteType;
    private User voteBy;
    private LocalDateTime voteOn;
}
