package com.example.project1.Controller;

import com.example.project1.Payload.Request.TaskRequest;
import com.example.project1.Payload.Request.TaskUpdateRequest;
import com.example.project1.Service.TasksService;
import com.example.project1.model.Tasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/tasks")
public class TasksController {
    @Autowired
     TasksService tasksService;

    @PostMapping("/add")
    public String loginSuccess(Model model, Authentication authentication, @RequestBody TaskRequest taskRequest) {
        String userEmail = authentication.getName();

        // Create the task for the logged-in user
        boolean taskCreated = tasksService.createTaskForEmail(userEmail, taskRequest);

        if (taskCreated) {
            // Task created successfully
            model.addAttribute("message", "Task created successfully.");
        } else {
            // Task creation failed
            model.addAttribute("message", "Failed to create the task.");
        }

        // Redirect to a success page or display a message to the user
        return "success-page";
    }
    @GetMapping("/all-tasks")
    public List<Tasks> getUserTasks(Authentication authentication) {
        // Get the currently logged-in user's email
        String userEmail = authentication.getName();

        // Retrieve tasks associated with the user's email
        List<Tasks> userTasks = tasksService.getTasksByEmail(userEmail);

        return userTasks;
    }
    @PutMapping("/update")
    public ResponseEntity<String> updateTask(@RequestBody TaskUpdateRequest updateRequest) {
        // You can include the task ID and completed status in TaskUpdateRequest.
        boolean taskUpdated =tasksService.updateTaskCompletion(updateRequest.getTaskId(), updateRequest.isCompleted());

        if (taskUpdated) {
            return ResponseEntity.ok("Task updated successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to update task.");
        }
    }
    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId, Authentication authentication) {

        String userEmail = authentication.getName(); // Get the currently logged-in user's email

        boolean taskDeleted = tasksService.deleteTaskByIdAndUserEmail(taskId, userEmail); // Call the service method to delete the task with the provided taskId and userEmail

        // Check the result and return an appropriate response
        if (taskDeleted) {
            return ResponseEntity.ok("Task deleted successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete the task.");
        }
    }


}
