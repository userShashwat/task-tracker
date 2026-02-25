package com.example.project1.Service;

import com.example.project1.Payload.Request.TaskRequest;
import com.example.project1.Repository.TasksRepository;
import com.example.project1.model.Tasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class TasksService {
    @Autowired
    TasksRepository taskRepository;

    // Other business logic...
    public boolean createTaskForEmail(String email, TaskRequest request) {
        Tasks task = new Tasks();
        task.setEmail(email);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCategory(request.getCategory());
        task.setCompleted(request.getCompleted());

        try {
            taskRepository.save(task);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Tasks> getTasksByEmail(String email) {
        return taskRepository.findByEmail(email);
    }

    public boolean updateTaskCompletion(Long taskId, boolean completed) {
        try {
            // Validate if taskId is not null
            if (taskId == null) {
                return false; // Invalid taskId
            }
            // Retrieve the task from the database by its ID
            Optional<Tasks> optionalTask = taskRepository.findById(taskId);

            if (optionalTask.isPresent()) {
                Tasks task = optionalTask.get();

                // Update the completed status
                task.setCompleted(completed);

                // Save the updated task back to the database
                taskRepository.save(task);

                return true; // Task updated successfully.
            } else {
                // Task with the given ID not found
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Failed to update the task.
        }
    }

    // Other service methods...
    public boolean deleteTaskByIdAndUserEmail(Long taskId, String userEmail) {
        try {
            // Retrieve the task from the database by its ID and user's email
            Optional<Tasks> optionalTask = taskRepository.findByIdAndEmail(taskId, userEmail);

            if (optionalTask.isPresent()) {
                Tasks task = optionalTask.get();

                // Delete the task from the database
                taskRepository.delete(task);

                return true; // Task deleted successfully.
            } else {
                // Task with the given ID and user's email not found
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Failed to delete the task.
        }
    }
}
