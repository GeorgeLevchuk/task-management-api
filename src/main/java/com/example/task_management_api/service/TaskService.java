package com.example.task_management_api.service;

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

    public List<Task> getAllTasks(){
        return taskRepository.findAll();
    }

    // GET - не нуждается в @Transactional, но можно оставить
    @Transactional(readOnly = true)  // Оптимизация для чтения
    public Task getById(Long id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)){
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
    // UPDATE (через Dirty Checking - это механизм Hibernate, который автоматически
    // отслеживает изменения в загруженных из БД объектах и синхронизирует их с базой
    // данных в конце транзакции без явного вызова save())
    public Task updateTask(Long id, Task updateTask) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        existing.setTitle(updateTask.getTitle());
        existing.setDescription(updateTask.getDescription());
        // return taskRepository.save(existing);
        // ❌ НЕ НУЖЕН save()! JPA сам сохранит

        return existing;
    }
}
