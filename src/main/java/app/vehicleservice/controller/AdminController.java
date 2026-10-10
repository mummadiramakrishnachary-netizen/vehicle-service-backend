package app.vehicleservice.controller;

import app.vehicleservice.dto.MechanicCreateRequest;
import app.vehicleservice.model.Mechanic;
import app.vehicleservice.model.Role;
import app.vehicleservice.model.User;
import app.vehicleservice.service.MechanicService;
import app.vehicleservice.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final MechanicService mechanicService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            UserService userService,
            MechanicService mechanicService,
            PasswordEncoder passwordEncoder) {

        this.userService = userService;
        this.mechanicService = mechanicService;
        this.passwordEncoder = passwordEncoder;
    }

    // Admin dashboard
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard() {
        return "Admin dashboard access granted";
    }

    // Admin: Create mechanic login account and mechanic profile
    @PostMapping("/mechanics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createMechanic(
            @Valid @RequestBody MechanicCreateRequest request) {

        // Check users table
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already registered as a user");
        }

        // Check mechanics table
        if (mechanicService.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already registered as a mechanic");
        }

        // Create login user
        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setRole(Role.MECHANIC);

        userService.saveUser(user);

        // Create mechanic profile
        Mechanic mechanic = new Mechanic(
                request.getName(),
                request.getEmail(),
                request.getPhone()
        );

        mechanicService.addMechanic(mechanic);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Mechanic account created successfully");
    }

    // Admin: Reset mechanic password
    @PutMapping("/mechanics/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> resetMechanicPassword(
            @RequestParam String email,
            @RequestParam String newPassword) {

        userService.resetPassword(
                email,
                newPassword
        );

        return ResponseEntity.ok(
                "Mechanic password reset successfully"
        );
    }
}