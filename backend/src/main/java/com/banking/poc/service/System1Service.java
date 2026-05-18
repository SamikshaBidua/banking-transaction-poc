package com.banking.poc.service;

import com.banking.poc.dto.TransactionRequest;
import com.banking.poc.dto.TransactionResponse;
import org.springframework.stereotype.Service;

@Service
public class System1Service {

    private final System2Service system2Service;

    public System1Service(System2Service system2Service) {
        this.system2Service = system2Service;
    }

    public TransactionResponse handleTransaction(TransactionRequest request, String userId) {
        String validationError = validateRequest(request);
        if (validationError != null) {
            return new TransactionResponse(false, validationError);
        }

        if (!isCardRangeSupported(request.getCardNumber())) {
            return new TransactionResponse(false, "Card range not supported");
        }

        return system2Service.processTransaction(
                request.getCardNumber(),
                request.getPin(),
                request.getAmount(),
                request.getType(),
                userId
        );
    }

    private String validateRequest(TransactionRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().isBlank()) {
            return "Card number is required";
        }
        if (request.getPin() == null || request.getPin().isBlank()) {
            return "PIN is required";
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return "Amount must be positive";
        }
        if (request.getType() == null ||
                (!request.getType().equalsIgnoreCase("withdraw") && !request.getType().equalsIgnoreCase("topup"))) {
            return "Type must be 'withdraw' or 'topup'";
        }
        return null;
    }

    private boolean isCardRangeSupported(String cardNumber) {
        return cardNumber.startsWith("4");
    }
}
