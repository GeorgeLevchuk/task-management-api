package com.example.task_management_api.service;

import com.example.task_management_api.dto.TaskPatchRequestDto;
import com.example.task_management_api.dto.TaskRequestDto;
import com.example.task_management_api.dto.TaskResponseDto;
import com.example.task_management_api.entity.Task;
import com.example.task_management_api.exception.TaskNotFoundException;
import com.example.task_management_api.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
    //methodName_condition_expectedResult
    void getAllTasks_whenTasksExist_returnsMappedDtos(){
        List<Task> list = new ArrayList<>();
        Task task = createTask(1L, "New title task", "New description task");
        list.add(task);
        Task task2 = createTask(2L, "New title task2", "New description task2");
        list.add(task2);
        Task task3 = createTask(3L, "New title task3", "New description task3");
        list.add(task3);

        when(taskRepository.findAll())
                .thenReturn(list);

        List<TaskResponseDto> listResponseDto = taskService.getAllTasks();

        assertEquals(1L, listResponseDto.get(0).getId());
        assertEquals("New title task", listResponseDto.get(0).getTitle());
        assertEquals("New description task", listResponseDto.get(0).getDescription());

        assertEquals(2L, listResponseDto.get(1).getId());
        assertEquals("New title task2", listResponseDto.get(1).getTitle());
        assertEquals("New description task2", listResponseDto.get(1).getDescription());

        assertEquals(3L, listResponseDto.get(2).getId());
        assertEquals("New title task3", listResponseDto.get(2).getTitle());
        assertEquals("New description task3", listResponseDto.get(2).getDescription());
        assertEquals(3, listResponseDto.size());

        verify(taskRepository, times(1)).findAll();
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void getAllTasks_whenNoTasks_returnsEmptyList(){
        List<Task> list = new ArrayList<>();

        when(taskRepository.findAll())
                .thenReturn(list);

        List<TaskResponseDto> listResponseDto = taskService.getAllTasks();

        assertNotNull(listResponseDto);
        assertTrue(listResponseDto.isEmpty());
        verify(taskRepository).findAll();
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void getById_whenTaskExists_returnsMappedDto(){
        Long id = 1L;
        Task task = createTask(id, "New title", "New description");

        when(taskRepository.findById(id))
                .thenReturn(Optional.of(task));

        TaskResponseDto responseDto = taskService.getById(id);

        assertNotNull(responseDto);
//обычно избыточно — компилятор уже гарантирует тип.
        assertInstanceOf(TaskResponseDto.class, responseDto);
        assertEquals(id, responseDto.getId());
        assertEquals("New title", responseDto.getTitle());
        assertEquals("New description", responseDto.getDescription());

        verify(taskRepository).findById(id);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void getById_whenTaskNotFound_throwsTaskNotFoundException(){

        Long id = 999L;
        when(taskRepository.findById(id))
                .thenReturn(Optional.empty());

        TaskNotFoundException ex = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getById(id)
        );

        assertTrue(ex.getMessage().contains(id + ""));

        verify(taskRepository).findById(id);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void createTask_whenValidRequest_savesEntityAndReturnsDto(){

        // Arrange
        TaskRequestDto request = new TaskRequestDto();
        request.setTitle("New title task");
        request.setDescription("New description task");

        Task task = createTask(2L, request.getTitle(), request.getDescription());

        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskResponseDto taskResponseDto = taskService.createTask(request);

        // Assert — результат
        assertEquals(2L, taskResponseDto.getId());
        assertEquals("New title task", taskResponseDto.getTitle());
        assertEquals("New description task", taskResponseDto.getDescription());


      //  verify(taskRepository).save(any(Task.class));

        //лучше проверять сам аргумент, а не факт вызова — например, через ArgumentCaptor
        //ArgumentCaptor - это класс из Mockito, который позволяет перехватить (захватить) аргументы, с которыми был вызван мок-метод, чтобы потом их детально проверить.
        //Проще говоря: это «ловушка» для аргументов. Ты говоришь моку: «когда тебя вызовут — положи переданный аргумент вот в эту коробочку», а потом открываешь коробочку
        // и изучаешь, что внутри.

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture()); //// ловим аргумент
        verifyNoMoreInteractions(taskRepository);

        Task sentToRepo = captor.getValue(); // достаём его
        assertEquals("New title task", sentToRepo.getTitle());
        assertEquals("New description task", sentToRepo.getDescription());

        //Зачем ArgumentCaptor нужен?
        //Проблема, которую он решает
        //Обычный verify проверяет только факт вызова

        //Инструмент	                    Что проверяет
        //verify(repo).save(any())	        Только факт вызова

        //verify(repo).save(argThat(...))	Факт + условие на аргумент (без сохранения)
      //  verify(taskRepository).save(argThat(t -> t.getTitle().equals("New title task")));

        //ArgumentCaptor	                Факт + полный доступ к переданному объекту для детальных проверок

    }

    @Test
    void deleteTask_whenTaskExists_deletesById(){
        Long id = 1L;

        when(taskRepository.existsById(id)).thenReturn(true);
        taskService.deleteTask(id);
//InOrder - это интерфейс из Mockito для проверки порядка вызовов методов на моках.
        InOrder inOrder = inOrder(taskRepository);
        inOrder.verify(taskRepository).existsById(id);
        inOrder.verify(taskRepository).deleteById(id);
        verify(taskRepository, times(1)).deleteById(id);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void deleteTask_whenTaskNotFound_throwsAndDoesNotDelete(){
        Long id = 2L;

        when(taskRepository.existsById(id)).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(id));
        verify(taskRepository).existsById(id);
        verify(taskRepository, never()).deleteById(id);
        verify(taskRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void updateTask_whenTaskExists_updatesFieldsAndSave(){
        Long id = 3L;
        Task task = createTask(id, "Old title", "Old description");

        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setTitle("New title");
        requestDto.setDescription("New description");

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        taskService.updateTask(id, requestDto);

        assertEquals("New title", task.getTitle());
        assertEquals("New description", task.getDescription());
        assertEquals(id, task.getId());

        verify(taskRepository).findById(id);
        verifyNoMoreInteractions(taskRepository);

        // save не вызывается: Hibernate сделает UPDATE автоматически
        // благодаря dirty checking в @Transactional-методе
       // verify(taskRepository).save(task);
    }

    @Test
    void updateTask_whenTaskNotFound_throwsAndDoesNotSave(){
        Long id = 4L;
        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setDescription("Create Rest Task API");
        requestDto.setTitle("Task API");

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask(id, requestDto));

        verify(taskRepository).findById(id);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void getById_whenTaskNotFound_throwsWithIdInMessage(){
        Long id = 999L;
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                ()->taskService.getById(id)
        );
        assertEquals("Task with id " + id + " not found",exception.getMessage());
    }

    @Test
    void updatePatchTask_whenOnlyTitleProvided_updatesTitleKeepsDescription(){
        Long id = 3L;
        Task task = createTask(id, "Old title", "Old description");

        TaskPatchRequestDto request = new TaskPatchRequestDto();
        request.setTitle("New title");

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        TaskResponseDto taskResponseDto = taskService.updatePatchTask(id, request);

        // состояние сущности
        assertEquals("New title",task.getTitle());
        assertEquals("Old description",task.getDescription());
        assertEquals(id, task.getId());

        // маппинг в DTO
        assertEquals("New title", taskResponseDto.getTitle());
        assertEquals("Old description", taskResponseDto.getDescription());

        verify(taskRepository).findById(id);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void updatePatchTask_whenUpdateDescriptionOnly(){
        Long id = 3L;
        Task task = createTask(id, "Old title", "Old description");

        TaskPatchRequestDto request = new TaskPatchRequestDto();
        request.setDescription("New description");

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        TaskResponseDto response = taskService.updatePatchTask(id, request);

        assertEquals("Old title", response.getTitle());
        assertEquals("New description", response.getDescription());
        verify(taskRepository).findById(id);


    }

    @Test
    void patchTask_whenOnlyDescriptionProvided_updatesDescriptionKeepsTitle(){
        Long id = 3L;
        Task task = createTask(id, "Old title", "Old description");

        TaskPatchRequestDto request = new TaskPatchRequestDto();
        request.setDescription("New description");
        // title намеренно null — имитируем «поле не пришло»

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        TaskResponseDto taskResponseDto = taskService.updatePatchTask(id, request);

        // состояние сущности
        assertEquals("Old title", task.getTitle());
        assertEquals("New description", task.getDescription());
        assertEquals(id, task.getId());

        // маппинг в DTO
        assertEquals("New title",taskResponseDto.getTitle());
        assertEquals("New description",taskResponseDto.getDescription());

        verify(taskRepository).findById(id);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void patchTask_whenNoFieldsProvided_keepsBothUnchanged() {
        Long id = 3L;
        Task task = createTask(id, "Old title", "Old description");

        TaskPatchRequestDto request = new TaskPatchRequestDto();
        // ни title, ни description не заданы — PATCH не должен ничего менять

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        TaskResponseDto response = taskService.updatePatchTask(id, request);

        // сущность не мутирована
        assertEquals("Old title", task.getTitle());
        assertEquals("Old description", task.getDescription());
        assertEquals(id, task.getId());

        // DTO содержит исходные значения
        assertEquals("Old title", response.getTitle());
        assertEquals("Old description", response.getDescription());

        verify(taskRepository).findById(id);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void patchTask_whenTaskNotFound_throwsAndDoesNotSave() {
        Long id = 3L;
        TaskPatchRequestDto request = new TaskPatchRequestDto();

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updatePatchTask(id, request)
        );

        verify(taskRepository).findById(id);
        verifyNoMoreInteractions(taskRepository);
    }



    //хелпер?это вспомогательный метод (или класс), который не является предметом теста,
    // а существует, чтобы уменьшить дублирование и сделать тесты читаемее.
    private Task createTask(Long id, String title, String description) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        return task;
    }
}
