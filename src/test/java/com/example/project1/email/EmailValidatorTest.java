package com.example.project1.email;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
//import static org.assertj.core.api.Assertions.assertThat;
public class EmailValidatorTest {
    private EmailValidator emailValidator;
    @BeforeEach
    void setUp(){
       emailValidator = new EmailValidator();
    }
    @Test
    @DisplayName("return true for all email")
    void testValidEmail(){
      assertThat(emailValidator.test("john@gmail.com")).isTrue();
        assertThat(emailValidator.test("user@example.com")).isTrue();
        assertThat(emailValidator.test("user+tag@example.com")).isTrue();
    }
    @Test
    @DisplayName("return false for invalid email")
    void testInvalidEmail(){
        assertThat(emailValidator.test("john@.com")).isFalse();
    }
}
