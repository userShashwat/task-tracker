package com.example.project1.email;

import org.springframework.stereotype.Service;

import java.util.function.Predicate;
import java.util.regex.Pattern;

@Service
public class EmailValidator implements Predicate<String> {

    private static final String REGEX_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    // Use static final for performance
    private static final Pattern PATTERN = Pattern.compile(REGEX_PATTERN);

    @Override
    public boolean test(String email) {
        if (email == null) {
            return false;
        }
        return PATTERN.matcher(email).matches();
    }
}