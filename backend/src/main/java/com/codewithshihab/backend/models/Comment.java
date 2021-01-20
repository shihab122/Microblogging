package com.codewithshihab.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment implements Serializable {
    private String description;
    private String commentOn;
    private User commentBy;
}
