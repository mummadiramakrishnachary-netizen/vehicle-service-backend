
package app.vehicleservice.controller;

import app.vehicleservice.dto.AdminBootstrapRequest;
import app.vehicleservice.model.Role;
import app.vehicleservice.model.User;
import app.vehicleservice.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AdminBootstrapController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapSecret;

    public AdminBootstrapController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin-bootstrap-secret:}")
            String bootstrapSecret) {

        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapSecret = bootstrapSecret;
    }

    @PostMapping("/bootstrap-admin")
    public ResponseEntity<String> bootstrapAdmin(
            @RequestHeader(
                value = "X-Admin-Bootstrap-Secret",
                required = false
            ) String suppliedSecret,
            @Valid @RequestBody AdminBootstrapRequest request) {

        if (bootstrapSecret.isBlank()
                || suppliedSecret == null
                || !MessageDigest.isEqual(
                    bootstrapSecret.getBytes(StandardCharsets.UTF_8),
                    suppliedSecret.getBytes(StandardCharsets.UTF_8))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Invalid or disabled bootstrap credentials");
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        User existing = userService.findByEmail(email).orElse(null);

        // Never promote an existing customer or staff account.
        if (existing != null && existing.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already belongs to a non-admin user");
        }

        // Only recover the specified admin account; don't replace
        // an unrelated administrator.
        boolean anotherAdminExists = userService.getAllUsers().stream()
                .anyMatch(user ->
                    user.getRole() == Role.ADMIN
                    && !user.getEmail().equalsIgnoreCase(email));

        if (anotherAdminExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Another admin exists; bootstrap refused");
        }

        User admin = existing != null ? existing : new User();

        admin.setName(request.getName().trim());
        admin.setEmail(email);
        admin.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        admin.setRole(Role.ADMIN);

        userService.saveUser(admin);

        return ResponseEntity.ok(
                "Admin initialized. Disable the bootstrap secret now."
        );
    }
}
