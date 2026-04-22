package com.example.project1.service;

import com.example.project1.Payload.Request.TaskRequest;
import com.example.project1.Repository.TasksRepository;
import com.example.project1.Service.TasksService;
import com.example.project1.model.Tasks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TasksServiceTest {
    @Mock
    TasksRepository tasksRepository;
    @InjectMocks
    private TasksService tasksService;
    private TaskRequest validTaskRequest;
    private Tasks validTask;
    private String testEmail;
    @BeforeEach
    void setUp(){
        testEmail = "test@example.com";
        validTaskRequest=new TaskRequest();
        validTaskRequest.setTitle("test task");
        validTaskRequest.setCategory("work");
        validTaskRequest.setDescription("first testing on process");
        validTaskRequest.setCompleted(false);
        validTask = new Tasks();
        validTask.setId(1L);
        validTask.setEmail(testEmail);
        validTask.setTitle("Test Task");
        validTask.setDescription("This is a test task");
        validTask.setCategory("Work");
        validTask.setCompleted(false);
    }
    @Test
    void createTask_True_WhenSucceeds(){
        // 1. Tell the mock what to do when save() is called
       

        when(tasksRepository.save(any(Tasks.class))).thenReturn(validTask);
        boolean result = tasksService.createTaskForEmail("test@example.com",validTaskRequest);
        assertThat(result).isTrue();
        verify(tasksRepository, times(1)).save(any(Tasks.class));
    }
    @Test
    void createTaskForEmail_ShouldReturnFalse_WhenSaveFails() {
        when(tasksRepository.save(any(Tasks.class))).thenThrow(new RuntimeException("db failed"));
        boolean result=tasksService.createTaskForEmail("user@example.com", validTaskRequest);
        assertThat(result).isFalse();
        verify(tasksRepository,times(1)).save(any(Tasks.class));
    }
    @Test
    void email_ReturnFalse_whenEmailNull(){
        boolean result=tasksService.createTaskForEmail(null, validTaskRequest);
        assertThat(result).isFalse();
        verify(tasksRepository,never()).save(any(Tasks.class));
    }
    @Test
    @DisplayName("Should return false when task request is null")
    void createTaskForEmail_ShouldReturnFalse_WhenTaskRequestIsNull(){
        boolean result=tasksService.createTaskForEmail("user@example.com", null);
        assertThat(result).isFalse();
        verify(tasksRepository,never()).save(any(Tasks.class));
    }
    @Test
    void  getTasksByEmail_returnTaskList_WhenTasksExist(){
        List<Tasks> expectedTask = Arrays.asList(validTask);
        when(tasksRepository.findByEmail(testEmail)).thenReturn(expectedTask);
        List<Tasks> actualTasks = tasksService.getTasksByEmail(testEmail);
        assertThat(actualTasks).isNotEmpty();        // 1. Not empty
        assertThat(actualTasks).hasSize(1);          // 2. Exactly 1 task
        assertThat(actualTasks.get(0).getEmail())    // 3. Email matches
                .isEqualTo(testEmail);
        verify(tasksRepository, times(1)).findByEmail(testEmail);
    }
}
