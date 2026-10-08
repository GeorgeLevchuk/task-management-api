package com.example.task_management_api.service;

import com.example.task_management_api.controller.TaskController;
import com.example.task_management_api.dto.ErrorResponseDto;
import com.example.task_management_api.dto.TaskRequestDto;
import com.example.task_management_api.dto.TaskResponseDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.example.task_management_api.exception.NotValidAttributeTaskException;
import com.example.task_management_api.exception.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(TaskController.class)//поднимает Spring-контекст (веб-слой), зависимости ищет как бины.
public class TaskControllerTest {

    @Autowired
    //MockMvc — это инструмент Spring для тестирования MVC-контроллеров, который позволяет выполнять имитацию HTTP-запросов и проверять HTTP-ответы без запуска полноценного веб-сервера.
    private MockMvc mockMvc;

    //Что такое Bean Validation?
     //Это стандартный механизм Java для проверки данных объектов по заданным ограничениям.


    @MockitoBean//Нужен @MockBean/@MockitoBean — он кладёт мок в контекст.
    private TaskService taskService;

    @Test
    void getAllTasks_whenTasksExist() throws Exception {
        TaskResponseDto taskResponseDto = new TaskResponseDto();
        taskResponseDto.setId(1L);
        taskResponseDto.setTitle("t");
        taskResponseDto.setDescription("d");

        TaskResponseDto taskResponseDto1 = new TaskResponseDto();
        taskResponseDto1.setId(2L);
        taskResponseDto1.setTitle("t1");
        taskResponseDto1.setDescription("d1");

        when(taskService.getAllTasks()).thenReturn(List.of(taskResponseDto,taskResponseDto1));

        mockMvc.perform(get("/tasks"))
                //Представим, что клиент отправил post-запрос на /tasks
                        .andExpect(status().isOk())
                //Я ожидаю, что Controller сформировал HTTP 200 OK
                        .andExpect(jsonPath("$.length()").value(2))
                        //проверяет содержимое JSON-ответа.
                        .andExpect(jsonPath("$[0].title").value("t"))
                        .andExpect(jsonPath("$[0].description").value("d"))
                        .andExpect(jsonPath("$[1].title").value("t1"))
                        .andExpect(jsonPath("$[1].description").value("d1"));

        verify(taskService).getAllTasks();
    }

    @Test
    void getTask_whenTaskFound() throws Exception {
        Long id =3L;

        TaskResponseDto taskResponseDto = new TaskResponseDto();
        taskResponseDto.setId(id);
        taskResponseDto.setTitle("t");
        taskResponseDto.setDescription("d");

        when(taskService.getById(id)).thenReturn(taskResponseDto);

        mockMvc.perform(get("/tasks/{id}",id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.description").value("d"))
                .andExpect(jsonPath("$.title").value("t"));

        verify(taskService, times(1)).getById(id);
    }

    @Test
    void getTask_whenTaskNotFound() throws Exception {
        Long id =3L;

        when(taskService.getById(id))
                .thenThrow(new TaskNotFoundException(id));

        mockMvc.perform(get("/tasks/{id}",id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id 3 not found"));

        verify(taskService).getById(id);
    }

    @Test
    void createTask_whenTaskFound() throws Exception {
        Long id =3L;

        TaskResponseDto taskResponseDto = new TaskResponseDto();
        taskResponseDto.setId(id);
        taskResponseDto.setTitle("t");
        taskResponseDto.setDescription("d");

        when(taskService.createTask(any(TaskRequestDto.class))).thenReturn(taskResponseDto);

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                //Content-Type указывает серверу, в каком формате передаётся тело HTTP-запроса
                .content("""
                        {
                            "title": "t",
                            "description": "d"
                        }
                        """))
                //Контент передает то, что будет находиться внутри теле запроса
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("d"))
                .andExpect(jsonPath("$.title").value("t"));

        verify(taskService).createTask(any(TaskRequestDto.class));
    }

    @Test
    void createTask_shouldReturnBadRequest_whenServiceThrowsValidationException() throws Exception {

        when(taskService.createTask(any(TaskRequestDto.class)))
                .thenThrow(new NotValidAttributeTaskException());

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "t",
                            "description": "d"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(taskService).createTask(any(TaskRequestDto.class));
    }

    @Test
    void createTask_shouldReturnBadRequest_whenTitleIsBlank() throws Exception {

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "title": "",
                        "description": "d"
                    }
                    """))
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .createTask(any(TaskRequestDto.class));
    }
}
