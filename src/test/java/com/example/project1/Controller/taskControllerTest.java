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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Test
    void createTask_Failure_ReturnsInternalServerError() throws Exception{
        when(authentication.getName()).thenReturn("john@example.com");
        when(tasksService.createTaskForEmail(anyString(),any(TaskRequest.class))).thenReturn(false);
        mockMvc.perform(post("/tasks/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest))
                .principal(authentication))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to create the task."));

    }
    @Test
    void getUserTasks_ReturnsListOfTasks() throws Exception{
        when(authentication.getName()).thenReturn("john@example.com");
        List<Tasks> tasksList=Arrays.asList(task);
        when(tasksService.getTasksByEmail("john@example.com")).thenReturn(tasksList);
        mockMvc.perform(get("/tasks/all-tasks")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

    }
    @Test
    void getUserTasks_NoTasks_ReturnsEmptyList() throws Exception{
        when(authentication.getName()).thenReturn("john@example.com");
        when(tasksService.getTasksByEmail("john@example.com")).thenReturn(Arrays.asList());
        mockMvc.perform(get("/tasks/all-tasks")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
    @Test
    void updateTask_Success_ReturnsOk() throws Exception{
        when(authentication.getName()).thenReturn("john@example.com");
        Long taskId=1L;
        Map<String,Boolean> update =new HashMap<>();
        update.put("completed",true);
        when(tasksService.updateTaskCompletionWithOwnership(eq(taskId), eq(true), eq("john@example.com"))).thenReturn(true);
        mockMvc.perform(put("/tasks/update/{taskId}",taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task updated successfully"));
    }


    @Test
    void updateTask_Failure_ReturnsForbidden() throws Exception {
        // Arrange
        Long taskId = 1L;
        Map<String, Boolean> updateRequest = new HashMap<>();
        updateRequest.put("completed", true);

        when(authentication.getName()).thenReturn("john@example.com");
        when(tasksService.updateTaskCompletionWithOwnership(eq(taskId), eq(true), eq("john@example.com")))
                .thenReturn(false);

        // Act & Assert - UPDATED for new code
        mockMvc.perform(put("/tasks/update/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .principal(authentication))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Task not found or access denied"));  // ← JSON assertion
    }
    @Test
    void deleteTask_Success_ReturnsOk() throws Exception {
        Long taskId = 1L;
        when(authentication.getName()).thenReturn("john@example.com");
        when(tasksService.deleteTaskByIdAndUserEmail(eq(taskId),eq("john@example.com"))).thenReturn(true);
        mockMvc.perform(delete("/tasks/delete/{taskId}", 1L)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Task deleted successfully."));

    }
    @Test
    void deleteTask_Failure_ReturnsBadRequest() throws Exception {
        // Arrange
        when(authentication.getName()).thenReturn("john@example.com");
        when(tasksService.deleteTaskByIdAndUserEmail(eq(1L), eq("john@example.com")))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/tasks/delete/{taskId}", 1L)
                        .principal(authentication))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to delete the task."));
    }



}