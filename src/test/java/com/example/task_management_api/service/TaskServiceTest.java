package com.example.task_management_api.service;

import com.example.task_management_api.dto.TaskResponseDto;
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

@ExtendWith(MockitoExtension.class)//Используй Mockito-аннотации в этом тестовом классе.
public class TaskServiceTest {

    @Mock//Создаёт поддельный TaskRepository.Это не настоящий Spring Bean и он не подключается к PostgreSQL.
    private TaskRepository taskRepository;

    @InjectMocks//Mockito создаёт TaskService и передаёт ему наш mock taskRepository.
    private TaskService taskService;

    @Test//Помечает метод как тестовый. JUnit запускает его автоматически.
    void shouldReturnTaskWhenTaskExists(){

        Task task = new Task();
        task.setId(1L);
        task.setDescription("Create Rest Task API");
        task.setTitle("Task API");

        //что это? - when(mock.метод(аргументы)).thenReturn(значение)
        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));
//        Когда taskRepository.findById(1L) будет вызван, верни Optional.of(task).
        //Когда Service попросит Repository найти Task с id 1 — верни эту Task».

        TaskResponseDto result = taskService.getById(1L);
        int n = 0;
        assertTrue(n >-2,"Present simple");//Этот метод проверяет, что переданное в него булево значение (condition) равно true
        //assertThrows();//этот метод нужен, чтобы проверить: выбрасывает ли блок кода (часто переданный через лямбду) исключение определённого типа. Если исключение есть и оно нужного типа (или подтип) — тест проходит. Если исключения нет вообще или тип не совпал — тест проваливается.

        //assertThrows(Class, executable) - Проверяет, что код выбрасывает ожидаемое исключение.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("Message");
        });

        assertEquals(task.getId(),result.getId());//Проверяет, что два значения равны. Если нет — тест падает.
   //     assertEquals("Learn Java", result.getTitle());
        assertEquals("Create Rest Task API", result.getDescription());

        // Для void-методов — doThrow / doNothing
     //   doThrow(new RuntimeException()).when(taskRepository).delete(any());
      //  doNothing().when(taskRepository).save(any());

// Матчеры (когда точное значение неизвестно)
        //   when(taskRepository.findByName(anyString())).thenReturn(user);
        // when(taskRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        //verify(...) — проверяем, что метод вызвался
    //    verify(taskRepository);

//        verify(userRepository).save(any(User.class));        // вызван 1 раз
//        verify(userRepository, times(2)).findById(1L);       // ровно 2 раза
//        verify(userRepository, never()).delete(any());       // ни разу
//        verify(userRepository, atLeastOnce()).save(user);    // минимум 1 раз
//        verify(userRepository, atLeast(2)).save(any());      // минимум 2
//        verify(userRepository, atMost(3)).save(any());       // максимум 3
//        verifyNoMoreInteractions(userRepository);            // больше вызовов не было
//        verifyNoInteractions(otherMock);                     // вообще не трогали

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnTaskWhenTaskNotExists(){

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getById(99L));

        verify(taskRepository).findById(99L);

        //что это?
//        when(taskRepository.findById(1L))
//                .thenReturn(Optional.empty());
//        Когда taskRepository.findById(1L) будет вызван, верни Optional.of(task).
        //Когда Service попросит Repository найти Task с id 1 — верни эту Task».

//        TaskResponseDto result = taskService.getById(1L);
//        int n = 0;
//        assertTrue(n >-2,"Present simple");//Этот метод проверяет, что переданное в него булево значение (condition) равно true
        //assertThrows();//этот метод нужен, чтобы проверить: выбрасывает ли блок кода (часто переданный через лямбду) исключение определённого типа. Если исключение есть и оно нужного типа (или подтип) — тест проходит. Если исключения нет вообще или тип не совпал — тест проваливается.

//        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
//            throw new IllegalArgumentException("Message");
//        });
//
//        assertEquals(task.getId(),result.getId());
//        //     assertEquals("Learn Java", result.getTitle());
//        assertEquals("Create Rest Task API", result.getDescription());
    }


}
