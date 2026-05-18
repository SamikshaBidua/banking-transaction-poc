package com.banking.poc.controller;

import com.banking.poc.dto.TransactionRequest;
import com.banking.poc.dto.TransactionResponse;
import com.banking.poc.model.Transaction;
import com.banking.poc.service.System1Service;
import com.banking.poc.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * System 1 - Transaction Routing API
 * Accepts transaction requests, performs validation, and routes to System 2 based on card range.
 */
@RestController
@RequestMapping("/api/system1/transactions")
public class System1Controller {

    private final System1Service system1Service;
    private final TransactionService transactionService;

    public System1Controller(System1Service system1Service, TransactionService transactionService) {
        this.system1Service = system1Service;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransactionResponse response = system1Service.handleTransaction(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_ADMIN".equals(role)) {
            return ResponseEntity.ok(transactionService.getAllTransactions());
        } else {
            return ResponseEntity.ok(transactionService.getTransactionsByUserId(userDetails.getUsername()));
        }
    }
}
