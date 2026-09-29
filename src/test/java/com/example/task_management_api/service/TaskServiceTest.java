package com.example.task_management_api.service;

import com.example.task_management_api.dto.TaskRequestDto;
import com.example.task_management_api.dto.TaskResponseDto;
import com.example.task_management_api.entity.Task;
import com.example.task_management_api.exception.TaskNotFoundException;
import com.example.task_management_api.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
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
    void getAll_whenTasksExists(){
        List<Task> list = new ArrayList<>();
        Task task = new Task();
        task.setId(1L);
        task.setTitle("New title task");
        task.setDescription("New description task");
        list.add(task);
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("New title task2");
        task2.setDescription("New description task2");
        list.add(task2);
        Task task3 = new Task();
        task3.setId(3L);
        task3.setTitle("New title task3");
        task3.setDescription("New description task3");
        list.add(task3);

        when(taskRepository.findAll())
                .thenReturn(list);

        List<TaskResponseDto> listResponseDto = taskService.getAllTasks();

        assertEquals("New title task", listResponseDto.get(0).getTitle());
        assertEquals("New description task", listResponseDto.get(0).getDescription());
        assertEquals("New title task2", listResponseDto.get(1).getTitle());
        assertEquals("New description task2", listResponseDto.get(1).getDescription());
        assertEquals("New title task3", listResponseDto.get(2).getTitle());
        assertEquals("New description task3", listResponseDto.get(2).getDescription());
        verify(taskRepository).findAll();
    }

    @Test
    void getAll_whenTasksNotExists(){
        List<Task> list = new ArrayList<>();

        when(taskRepository.findAll())
                .thenReturn(list);

        List<TaskResponseDto> listResponseDto = taskService.getAllTasks();

        assertEquals(0, listResponseDto.size());
        verify(taskRepository).findAll();
    }

    @Test
    void getById_whenTaskFound(){
        Long id = 1L;
        Task task = new Task();
        task.setId(id);
        task.setTitle("New title");
        task.setDescription("New description");

        when(taskRepository.findById(id))
                .thenReturn(Optional.of(task));

        TaskResponseDto responseDto = taskService.getById(id);

        assertEquals("New title", responseDto.getTitle());
        assertEquals("New description", responseDto.getDescription());
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
    void createTask_whenTaskCreated(){

        TaskRequestDto request = new TaskRequestDto();
        request.setTitle("New title task");
        request.setDescription("New description task");

        Task task = new Task();
        task.setId(2L);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponseDto taskResponseDto = taskService.createTask(request);

        assertEquals("New title task", taskResponseDto.getTitle());
        assertEquals("New description task", taskResponseDto.getDescription());
        verify(taskRepository).save(any());
        verify(taskRepository).save(any(Task.class));

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
    void exceptionGetMessage_TaskNotFoundException(){
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                ()->taskService.getById(999L)
        );
        assertEquals("Task with id 999 not found",exception.getMessage());
    }


}
