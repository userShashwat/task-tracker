package com.example.project1.service;

import com.example.project1.Payload.Request.TaskRequest;
import com.example.project1.Repository.TasksRepository;
import com.example.project1.Service.TasksService;
import com.example.project1.model.Tasks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

        validTaskRequest = new TaskRequest();
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
    @DisplayName("Should return true when task is created successfully")
    void createTask_True_WhenSucceeds(){
        when(tasksRepository.save(any(Tasks.class))).thenReturn(validTask);

        boolean result = tasksService.createTaskForEmail("test@example.com", validTaskRequest);

        assertThat(result).isTrue();
        verify(tasksRepository, times(1)).save(any(Tasks.class));
    }

    @Test
    @DisplayName("Should return false when save fails")
    void createTaskForEmail_ShouldReturnFalse_WhenSaveFails() {
        when(tasksRepository.save(any(Tasks.class))).thenThrow(new RuntimeException("db failed"));

        boolean result = tasksService.createTaskForEmail("user@example.com", validTaskRequest);

        assertThat(result).isFalse();
        verify(tasksRepository, times(1)).save(any(Tasks.class));
    }

    @Test
    @DisplayName("Should return false when email is null")
    void createTaskForEmail_ShouldReturnFalse_WhenEmailIsNull() {
        boolean result = tasksService.createTaskForEmail(null, validTaskRequest);

        assertThat(result).isFalse();
        verify(tasksRepository, never()).save(any(Tasks.class));
    }

    @Test
    @DisplayName("Should return false when task request is null")
    void createTaskForEmail_ShouldReturnFalse_WhenTaskRequestIsNull(){
        boolean result = tasksService.createTaskForEmail("user@example.com", null);

        assertThat(result).isFalse();
        verify(tasksRepository, never()).save(any(Tasks.class));
    }

    @Test
    @DisplayName("Should return task list when tasks exist")
    void getTasksByEmail_ReturnTaskList_WhenTasksExist(){
        List<Tasks> expectedTask = Arrays.asList(validTask);
        when(tasksRepository.findByEmail(testEmail)).thenReturn(expectedTask);

        List<Tasks> actualTasks = tasksService.getTasksByEmail(testEmail);

        assertThat(actualTasks).isNotEmpty();
        assertThat(actualTasks).hasSize(1);
        assertThat(actualTasks.get(0).getEmail()).isEqualTo(testEmail);
        verify(tasksRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Should return empty list when email is null")
    void getTasksByEmail_ShouldReturnEmptyList_WhenEmailIsNull(){
        List<Tasks> actualTasks = tasksService.getTasksByEmail(null);

        assertThat(actualTasks).isEmpty();
        verify(tasksRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Should return empty list when no tasks exist")
    void getTasksByEmail_ShouldReturnEmptyList_WhenNoTasksExist(){
        List<Tasks> expectedTask = Arrays.asList();
        when(tasksRepository.findByEmail(testEmail)).thenReturn(expectedTask);

        List<Tasks> actualTasks = tasksService.getTasksByEmail(testEmail);

        assertThat(actualTasks).isEmpty();
        verify(tasksRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Should return true when task is updated successfully")
    void updateTaskCompletion_ShouldReturnTrue_WhenTaskExists(){
        when(tasksRepository.findByIdAndEmail(1L, testEmail)).thenReturn(Optional.of(validTask));
        when(tasksRepository.save(any(Tasks.class))).thenReturn(validTask);

        boolean result = tasksService.updateTaskCompletionWithOwnership(1L, true, testEmail);

        assertThat(result).isTrue();
        assertThat(validTask.getCompleted()).isTrue();
        verify(tasksRepository, times(1)).findByIdAndEmail(1L, testEmail);
        verify(tasksRepository, times(1)).save(any(Tasks.class));
    }

    @Test
    @DisplayName("Should return false when task does not exist")
    void updateTaskCompletion_ShouldReturnFalse_WhenTaskDoesNotExist(){
        when(tasksRepository.findByIdAndEmail(999L, testEmail)).thenReturn(Optional.empty());

        boolean result = tasksService.updateTaskCompletionWithOwnership(999L, true, testEmail);

        assertThat(result).isFalse();
        verify(tasksRepository, times(1)).findByIdAndEmail(999L, testEmail);
        verify(tasksRepository, never()).save(any(Tasks.class));
    }

    @Test
    @DisplayName("Should return false when task ID is null")
    void updateTaskCompletion_ShouldReturnFalse_WhenIdIsNull(){
        boolean result = tasksService.updateTaskCompletionWithOwnership(null, true, testEmail);

        assertThat(result).isFalse();
        verify(tasksRepository, never()).findByIdAndEmail(any(), any());
        verify(tasksRepository, never()).save(any(Tasks.class));
    }

    @Test
    @DisplayName("Should change status from false to true when updated")
    void updateTaskCompletion_ShouldChangeStatusFromFalseToTrue(){
        Tasks freshTask = new Tasks();
        freshTask.setId(1L);
        freshTask.setEmail(testEmail);
        freshTask.setTitle("Test Task");
        freshTask.setDescription("Test Description");
        freshTask.setCategory("Work");
        freshTask.setCompleted(false);
        when(tasksRepository.findByIdAndEmail(1L, testEmail)).thenReturn(Optional.of(freshTask));
        when(tasksRepository.save(any(Tasks.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = tasksService.updateTaskCompletionWithOwnership(1L, true, testEmail);

        assertThat(result).isTrue();
        assertThat(freshTask.getCompleted()).isTrue();
        verify(tasksRepository, times(1)).findByIdAndEmail(1L, testEmail);
        verify(tasksRepository, times(1)).save(freshTask);  // ← Save was called with freshTask
    }
    @Test
    @DisplayName("Should return true when task is deleted successfully")
    void deleteTaskByIdAndUserEmail_ShouldReturnTrue_WhenTaskExists(){
        when(tasksRepository.findByIdAndEmail(1L, testEmail)).thenReturn(Optional.of(validTask));
        doNothing().when(tasksRepository).delete(validTask);

        boolean result = tasksService.deleteTaskByIdAndUserEmail(1L, testEmail);

        assertThat(result).isTrue();
        verify(tasksRepository, times(1)).findByIdAndEmail(1L, testEmail);
        verify(tasksRepository, times(1)).delete(validTask);
    }

    @Test
    @DisplayName("Should return false when task does not exist for user")
    void deleteTaskByIdAndUserEmail_ShouldReturnFalse_WhenTaskDoesNotExist(){
        when(tasksRepository.findByIdAndEmail(999L, testEmail)).thenReturn(Optional.empty());

        boolean result = tasksService.deleteTaskByIdAndUserEmail(999L, testEmail);

        assertThat(result).isFalse();
        verify(tasksRepository, times(1)).findByIdAndEmail(999L, testEmail);
        verify(tasksRepository, never()).delete(any(Tasks.class));
    }
}