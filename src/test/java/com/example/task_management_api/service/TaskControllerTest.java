package com.example.task_management_api.service;

import com.example.task_management_api.controller.TaskController;
import com.example.task_management_api.dto.TaskResponseDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ExtendWith(MockitoExtension.class) //почему @WebMvcTest + @Mock — несовместимы?
@WebMvcTest(TaskController.class)//что это?
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;//что это?

   // @Mock
    @MockitoBean//что это?
    private TaskService taskService;

//    @InjectMocks
//    private TaskController taskController;

    @Test
    void getAllTasks_whenTaskFound() throws Exception {
      //  List<TaskResponseDto> list = new ArrayList<>();
        TaskResponseDto taskResponseDto = new TaskResponseDto();
        taskResponseDto.setId(1L);
        taskResponseDto.setTitle("t");
        taskResponseDto.setDescription("d");
       // list.add(taskResponseDto);
        TaskResponseDto taskResponseDto1 = new TaskResponseDto();
        taskResponseDto1.setId(2L);
        taskResponseDto1.setTitle("t1");
        taskResponseDto1.setDescription("d1");
       // list.add(taskResponseDto1);

        when(taskService.getAllTasks()).thenReturn(List.of(taskResponseDto,taskResponseDto1));
//        List<TaskResponseDto> listDto = taskController.getAllTasks();
//
//        assertEquals("t",listDto.get(0).getTitle());
//        assertEquals("d",listDto.get(0).getDescription());
//        assertEquals("t1",listDto.get(1).getTitle());
//        assertEquals("d1",listDto.get(1).getDescription());

        //что здесь поисходит?
        mockMvc.perform(get("/tasks"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2))
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

        //что здесь поисходит?
        mockMvc.perform(get("/tasks/{id}",id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.description").value("d"))
                .andExpect(jsonPath("$.title").value("t"));

        verify(taskService, times(1)).getById(id);
    }
}
