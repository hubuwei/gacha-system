package com.cheng.cms.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("\nSQL to update:");
        System.out.println("UPDATE admins SET password_hash = '" + hash + "' WHERE username='admin';");
    }
}
