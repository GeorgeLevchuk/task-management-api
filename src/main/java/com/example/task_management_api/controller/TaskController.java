package com.example.task_management_api.controller;

import com.example.task_management_api.dto.TaskPatchRequestDto;
import com.example.task_management_api.dto.TaskRequestDto;
import com.example.task_management_api.dto.TaskResponseDto;
import com.example.task_management_api.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponseDto> getAllTasks(){
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public TaskResponseDto getTask(@PathVariable Long id){
        return taskService.getById(id);
    }

    @PostMapping
        public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto request){
        TaskResponseDto createdTask = taskService.createTask(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
        return ResponseEntity
                .noContent()
                .build();
    }

    @PutMapping("/{id}")
    public TaskResponseDto updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDto request){
        return taskService.updateTask(id,request);
    }

    @PatchMapping("/{id}")
    public TaskResponseDto updatePatchTask(@PathVariable Long id, @Valid @RequestBody TaskPatchRequestDto request){return taskService.updatePatchTask(id,request);}
}
