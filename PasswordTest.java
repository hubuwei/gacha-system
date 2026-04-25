import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String storedHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8QeN3jYpCvnFzJHkH9gPZQwLxK5iG";
        String testPassword = "admin123";
        
        System.out.println("Testing password: " + testPassword);
        System.out.println("Stored hash: " + storedHash);
        System.out.println("Matches: " + encoder.matches(testPassword, storedHash));
        
        // Also test some other common passwords
        String[] testPasswords = {"admin", "123456", "password", "admin123456"};
        for (String pwd : testPasswords) {
            System.out.println(pwd + " matches: " + encoder.matches(pwd, storedHash));
        }
    }
}
