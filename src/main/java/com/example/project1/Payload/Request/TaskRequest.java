package com.example.project1.Payload.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class TaskRequest {
    private String title;
    private String description;
    private String category;
    private Boolean completed;

    // Getters and setters...

}
