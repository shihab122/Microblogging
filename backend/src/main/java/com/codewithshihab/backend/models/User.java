package com.codewithshihab.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.geometry.Pos;
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
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private Name name;

    private LocalDateTime createdAt;

    private List<String> emailList;

    private List<String> phoneNumberList;

    // Common attributes for all model class
    @Indexed
    private List<ActivityFeed> activityFeedList;


}
