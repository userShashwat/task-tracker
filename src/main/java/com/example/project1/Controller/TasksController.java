package com.example.project1.Controller;

import com.example.project1.Payload.Request.TaskRequest;
import com.example.project1.Payload.Request.TaskUpdateRequest;
import com.example.project1.Service.TasksService;
import com.example.project1.model.Tasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TasksController {

    @Autowired
    private TasksService tasksService;

    @PostMapping("/add")
    public ResponseEntity<String> createTask(Authentication authentication,
                                             @RequestBody TaskRequest taskRequest) {
        // Get email from authenticated user
        String userEmail = authentication.getName();

        // Create the task for the logged-in user
        boolean taskCreated = tasksService.createTaskForEmail(userEmail, taskRequest);

        if (taskCreated) {
            return ResponseEntity.ok("Task created successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create the task.");
        }
    }

    @GetMapping("/all-tasks")
    public ResponseEntity<List<Tasks>> getUserTasks(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Tasks> userTasks = tasksService.getTasksByEmail(userEmail);
        return ResponseEntity.ok(userTasks);
    }

    @PutMapping("/update/{taskId}")
    public ResponseEntity<?> updateTaskCompletion(
            @PathVariable Long taskId,
            @RequestBody Map<String, Boolean> update,
            Authentication authentication) {

        String userEmail = authentication.getName();
        Boolean completed = update.get("completed");

        if (completed == null) {
            return ResponseEntity.badRequest().body("Missing 'completed' field");
        }

        boolean taskUpdated = tasksService.updateTaskCompletionWithOwnership(
                taskId, completed, userEmail);

        if (taskUpdated) {
            return ResponseEntity.ok(Map.of(
                    "message", "Task updated successfully",
                    "taskId", taskId,
                    "completed", completed
            ));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Task not found or access denied"));
        }
    }

    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId,
                                             Authentication authentication) {
        String userEmail = authentication.getName();
        boolean taskDeleted = tasksService.deleteTaskByIdAndUserEmail(taskId, userEmail);

        if (taskDeleted) {
            return ResponseEntity.ok("Task deleted successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete the task.");
        }
    }
}