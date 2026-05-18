package com.banking.poc.controller;

import com.banking.poc.dto.TransactionRequest;
import com.banking.poc.dto.TransactionResponse;
import com.banking.poc.service.System2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * System 2 - Card Validation & Processing API
 * Validates card details, authenticates PIN, checks/updates balance.
 * Called internally by System 1, but also exposed as a separate REST API.
 */
@RestController
@RequestMapping("/api/system2/process")
public class System2Controller {

    private final System2Service system2Service;

    public System2Controller(System2Service system2Service) {
        this.system2Service = system2Service;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> processTransaction(
            @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransactionResponse response = system2Service.processTransaction(
                request.getCardNumber(),
                request.getPin(),
                request.getAmount(),
                request.getType(),
                userDetails.getUsername()
        );
        return ResponseEntity.ok(response);
    }
}
