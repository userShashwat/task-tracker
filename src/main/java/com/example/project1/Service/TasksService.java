package com.example.project1.Service;

import com.example.project1.Payload.Request.TaskRequest;
import com.example.project1.Repository.TasksRepository;
import com.example.project1.model.Tasks;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TasksService {

    @Autowired
    TasksRepository taskRepository;

    // FIXED: Added null checks
    public boolean createTaskForEmail(String email, TaskRequest request) {
        // NULL CHECK #1
        if (email == null || request == null) {
            return false;
        }

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

    // FIXED: Added null check
    public List<Tasks> getTasksByEmail(String email) {
        // NULL CHECK #2
        if (email == null) {
            return new ArrayList<>();  // Return empty list instead of null
        }
        return taskRepository.findByEmail(email);
    }

    // FIXED: Added null check
    @Transactional
    public boolean updateTaskCompletionWithOwnership(Long taskId, boolean completed, String userEmail) {
        // Use repository method with ownership check
        Optional<Tasks> optionalTask = taskRepository.findByIdAndEmail(taskId, userEmail);

        if (optionalTask.isEmpty()) {
            return false;  // Task doesn't exist or doesn't belong to user
        }

        Tasks task = optionalTask.get();
        task.setCompleted(completed);
        taskRepository.save(task);
        return true;
    }

    // FIXED: Added null checks
    public boolean deleteTaskByIdAndUserEmail(Long taskId, String userEmail) {
        // NULL CHECK #4
        if (taskId == null || userEmail == null) {
            return false;
        }

        try {
            Optional<Tasks> optionalTask = taskRepository.findByIdAndEmail(taskId, userEmail);

            if (optionalTask.isPresent()) {
                Tasks task = optionalTask.get();
                taskRepository.delete(task);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}