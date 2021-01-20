package com.codewithshihab.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
public class Post implements Serializable {
    @Id
    private String id;

    private String description;

    private LocalDateTime postedAt;

    private LocalDateTime postedBy;

    private List<Review> reviewList;

    // Common attributes for all model class
    @Indexed
    private List<ActivityFeed> activityFeedList;

}
