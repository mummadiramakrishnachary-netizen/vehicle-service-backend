package app.vehicleservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-advisor")
public class ServiceAdvisorController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SERVICE_ADVISOR')")
    public String dashboard() {
        return "Service advisor dashboard access granted";
    }
}