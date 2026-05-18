package com.banking.poc.dto;

public class TransactionResponse {
    private boolean success;
    private String message;
    private String transactionId;
    private Double newBalance;

    public TransactionResponse() {}

    public TransactionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public TransactionResponse(boolean success, String message, String transactionId, Double newBalance) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
        this.newBalance = newBalance;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Double getNewBalance() { return newBalance; }
    public void setNewBalance(Double newBalance) { this.newBalance = newBalance; }
}
