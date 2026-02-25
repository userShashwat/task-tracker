package com.example.project1.Repository;

import com.example.project1.model.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TasksRepository extends JpaRepository<Tasks,Long> {
    List<Tasks> findByEmail(String email);
    Optional<Tasks> findByIdAndEmail(Long Id,String userEmail);
}
