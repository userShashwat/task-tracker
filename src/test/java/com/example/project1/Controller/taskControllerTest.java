package com.example.project1.Controller;

import com.example.project1.Payload.Request.TaskRequest;
import com.example.project1.Payload.Request.TaskUpdateRequest;
import com.example.project1.Service.TasksService;
import com.example.project1.model.Tasks;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TasksControllerTest {
    //simulate http request
    private MockMvc mockMvc;

    @Mock
    private TasksService tasksService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TasksController tasksController;
    // Converts Java to JSON
    private ObjectMapper objectMapper = new ObjectMapper();
    private TaskRequest taskRequest;
    private TaskUpdateRequest updateRequest;
    private Tasks task;
    @BeforeEach
    void setUp(){
        mockMvc=MockMvcBuilders.standaloneSetup(tasksController).build();
        taskRequest = new TaskRequest();
        taskRequest.setTitle("Learn Testing");
        taskRequest.setDescription("Write unit tests for the project");
        taskRequest.setCategory("Learning");
        taskRequest.setCompleted(false);
        updateRequest = new TaskUpdateRequest();
        updateRequest.setTaskId(1L);
        updateRequest.setCompleted(true);
        // Sample task
        task = new Tasks();
        task.setId(1L);
        task.setTitle("Learn Testing");
        task.setDescription("Write unit tests");
        task.setCategory("Learning");
        task.setCompleted(false);
        task.setEmail("john@example.com");
    }
    @Test
    void createTask_Success_ReturnOk () throws Exception{
     when(authentication.getName()).thenReturn("john@example.com");
     when(tasksService.createTaskForEmail(eq("john@example.com"),any(TaskRequest.class))).thenReturn(true);
     mockMvc.perform(
             post("/tasks/add")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(objectMapper.writeValueAsString(taskRequest))
                     .principal(authentication))
             .andExpect(status().isOk())
             .andExpect(content().string("Task created successfully."));
        verify(tasksService, times(1)).createTaskForEmail(anyString(), any(TaskRequest.class));

    }

}