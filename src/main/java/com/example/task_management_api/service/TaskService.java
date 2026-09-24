package com.example.task_management_api.service;

import com.example.task_management_api.dto.TaskPatchRequestDto;
import com.example.task_management_api.dto.TaskRequestDto;
import com.example.task_management_api.dto.TaskResponseDto;
import com.example.task_management_api.entity.Task;
import com.example.task_management_api.exception.TaskNotFoundException;
import com.example.task_management_api.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    public List<TaskResponseDto> getAllTasks(){
        List<Task> tasks = taskRepository.findAll();
        List<TaskResponseDto> responses = new java.util.ArrayList<>();
        for (Task task : tasks) {
            TaskResponseDto response = new TaskResponseDto();
            response.setId(task.getId());
            response.setTitle(task.getTitle());
            response.setDescription(task.getDescription());
            responses.add(response);
        }
        return responses;
        //позже через Stream
    }

    public TaskResponseDto getById(Long id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        TaskResponseDto response = new TaskResponseDto();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        return response;
    }

    public TaskResponseDto createTask(TaskRequestDto request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        Task savedTask = taskRepository.save(task);

        TaskResponseDto response = new TaskResponseDto();
        response.setId(savedTask.getId());
        response.setTitle(savedTask.getTitle());
        response.setDescription(savedTask.getDescription());

        return response;
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)){
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    public TaskResponseDto updateTask(Long id, TaskRequestDto request) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());

        TaskResponseDto response = new TaskResponseDto();
        response.setId(existing.getId());
        response.setTitle(existing.getTitle());
        response.setDescription(existing.getDescription());
        return response;
    }

    public TaskResponseDto  updatePatchTask(Long id, TaskPatchRequestDto request) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }

        TaskResponseDto response = new TaskResponseDto();

        response.setId(existing.getId());
        response.setTitle(existing.getTitle());
        response.setDescription(existing.getDescription());

        return response;
    }
}
