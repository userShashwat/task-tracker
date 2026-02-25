package com.example.project1.Repository;

import com.example.project1.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
// Add this import at the top of your file
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface UsersRepository extends JpaRepository<Users,Integer>{
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email); //checks if a user with the given email exists in the database.
    void deleteByEmail(String email);  // Custom method to delete a user by email
    @Transactional
    @Modifying
    @Query("UPDATE Users a " + "SET a.verified = TRUE WHERE a.email = ?1")
    int verifyUser(String email);
    @Query("SELECT verified FROM Users WHERE email = ?1")
    boolean isVerifiedUser(String email);

}
