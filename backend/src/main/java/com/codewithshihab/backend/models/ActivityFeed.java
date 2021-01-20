package com.codewithshihab.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityFeed implements Serializable {
    @Id
    private String id;

    // @{user:user_id:user name} was created
    @Indexed
    private String title;

    private String description;

    @DBRef
    @Indexed
    private User actionBy;

    @Indexed
    private LocalDateTime actionOn;

    @Indexed
    private String actionFrom;  // IP Address
}

