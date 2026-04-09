package com.saas.analytics.controller;

import com.saas.analytics.model.User;
import com.saas.analytics.repository.UserRepository;
import com.saas.analytics.service.IntegrationService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {

    private final IntegrationService integrationService;
    private final UserRepository userRepository;

    public IntegrationController(IntegrationService integrationService, UserRepository userRepository) {
        this.integrationService = integrationService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    }

    @PostMapping("/meta")
    public ResponseEntity<?> addMetaAccount(@RequestBody MetaRequest request) {
        User user = getAuthenticatedUser();
        integrationService.addMetaAccount(user, request.getAccountName(), request.getAdAccountId(), request.getAccessToken(), request.getCurrency());
        return ResponseEntity.ok("Meta account integrated successfully.");
    }

    @PostMapping("/shopify")
    public ResponseEntity<?> addShopifyStore(@RequestBody ShopifyRequest request) {
        User user = getAuthenticatedUser();
        integrationService.addShopifyStore(user, request.getStoreName(), request.getStoreUrl(), request.getClientId(), request.getClientSecret());
        return ResponseEntity.ok("Shopify store integrated successfully.");
    }
    
    @GetMapping("/meta")
    public ResponseEntity<?> getMetaAccounts() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(integrationService.getUserMetaAccounts(user.getId()));
    }

    @GetMapping("/shopify")
    public ResponseEntity<?> getShopifyStores() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(integrationService.getUserShopifyStores(user.getId()));
    }

    @DeleteMapping("/meta/{id}")
    public ResponseEntity<?> deleteMetaAccount(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        integrationService.deleteMetaAccount(id, user.getId());
        return ResponseEntity.ok("Deleted");
    }

    @DeleteMapping("/shopify/{id}")
    public ResponseEntity<?> deleteShopifyStore(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        integrationService.deleteShopifyStore(id, user.getId());
        return ResponseEntity.ok("Deleted");
    }

    @Data
    static class MetaRequest {
        private String accountName;
        private String adAccountId;
        private String accessToken;
        private String currency;
    }

    @Data
    static class ShopifyRequest {
        private String storeName;
        private String storeUrl;
        private String clientId;
        private String clientSecret;
    }
}
