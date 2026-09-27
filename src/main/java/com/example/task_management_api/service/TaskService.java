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
//        List<Task> tasks = taskRepository.findAll();
//        List<TaskResponseDto> responses = new java.util.ArrayList<>();
//        for (Task task : tasks) {
//            TaskResponseDto response = mapToResponse(task);
//            responses.add(response);
//        }
//        return responses;

        return taskRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponseDto getById(Long id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return mapToResponse(task);

    }

    public TaskResponseDto createTask(TaskRequestDto request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask);

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

        return mapToResponse(existing);

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

        return mapToResponse(existing);
    }

    private TaskResponseDto mapToResponse(Task task){
        TaskResponseDto taskResponseDto = new TaskResponseDto();
        taskResponseDto.setId(task.getId());
        taskResponseDto.setDescription(task.getDescription());
        taskResponseDto.setTitle(task.getTitle());

        return taskResponseDto;
    }


}
