package com.example.task_management_api.dto;

import jakarta.validation.constraints.Size;

public class TaskPatchRequestDto {

    @Size(max = 255)
    private String title;
    private String description;

    public TaskPatchRequestDto() { }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
