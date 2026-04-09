package com.saas.analytics.controller;

import com.saas.analytics.model.User;
import com.saas.analytics.repository.UserRepository;
import com.saas.analytics.service.EtlService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/etl")
public class EtlController {

    private final EtlService etlService;
    private final UserRepository userRepository;

    public EtlController(EtlService etlService, UserRepository userRepository) {
        this.etlService = etlService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    }

    @PostMapping("/trigger")
    public ResponseEntity<?> triggerEtl() {
        try {
            User user = getAuthenticatedUser();
            etlService.processUser(user);
            return ResponseEntity.ok("Sync completed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Sync Failed: " + e.getMessage());
        }
    }
}
