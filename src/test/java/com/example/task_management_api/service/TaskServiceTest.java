package com.example.task_management_api.service;

import com.example.task_management_api.dto.TaskRequestDto;
import com.example.task_management_api.entity.Task;
import com.example.task_management_api.exception.TaskNotFoundException;
import com.example.task_management_api.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldReturnTaskWhenTaskExists(){

        Task task = new Task();
        task.setId(1L);
        task.setDescription("Create Rest Task API");
        task.setTitle("Task API");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnTaskWhenTaskNotExists(){
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getById(99L));
        verify(taskRepository).findById(99L);
    }

    @Test
    void deleteTaskExistsById(){
        Long id = 1L;

        when(taskRepository.existsById(id)).thenReturn(true);
        taskService.deleteTask(id);

        verify(taskRepository).existsById(id);
        verify(taskRepository).deleteById(id);
    }

    @Test

    void deleteTaskNotExistsById(){
        Long id = 2L;

        when(taskRepository.existsById(id)).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(id));
        verify(taskRepository, never()).deleteById(id);
    }

    @Test
    void updateTaskExistsById(){
        Task task = new Task();
        task.setId(3L);
        task.setTitle("Old title");
        task.setDescription("Old description");

        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setTitle("New title");
        requestDto.setDescription("New description");

        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));

        taskService.updateTask(3L, requestDto);

        assertEquals("New title", task.getTitle());
        assertEquals("New description", task.getDescription());
        verify(taskRepository, times(1)).findById(3L);
        verify(taskRepository).findById(3L);
    }

    @Test
    void updateTaskNotExistsById(){
        Long id = 4L;
        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setDescription("Create Rest Task API");
        requestDto.setTitle("Task API");

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask(id, requestDto));
        verify(taskRepository).findById(id);
    }

    @Test
    void getById_shouldThrowException_whenTaskNotFound(){
        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getById(999L)
        );
    }

    @Test
    void exceptionGetMessage_TaskNotFoundException(){
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                ()->taskService.getById(999L)
        );
        assertEquals("Task with id 999 not found",exception.getMessage());
    }


}
