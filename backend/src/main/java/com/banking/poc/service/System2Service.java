package com.banking.poc.service;

import com.banking.poc.dto.TransactionResponse;
import com.banking.poc.model.Card;
import com.banking.poc.model.Transaction;
import com.banking.poc.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class System2Service {

    private final CardService cardService;
    private final TransactionRepository transactionRepository;

    public System2Service(CardService cardService, TransactionRepository transactionRepository) {
        this.cardService = cardService;
        this.transactionRepository = transactionRepository;
    }

    public TransactionResponse processTransaction(String cardNumber, String pin, double amount, String type, String userId) {
        Optional<Card> cardOpt = cardService.findByCardNumber(cardNumber);

        if (cardOpt.isEmpty()) {
            Transaction txn = new Transaction(cardNumber, type, amount, "DECLINED", "Invalid card", userId);
            transactionRepository.save(txn);
            return new TransactionResponse(false, "Invalid card");
        }

        Card card = cardOpt.get();

        if (!cardService.validatePin(card, pin)) {
            Transaction txn = new Transaction(cardNumber, type, amount, "DECLINED", "Invalid PIN", userId);
            transactionRepository.save(txn);
            return new TransactionResponse(false, "Invalid PIN");
        }

        if ("withdraw".equalsIgnoreCase(type)) {
            if (!cardService.hasSufficientBalance(card, amount)) {
                Transaction txn = new Transaction(cardNumber, type, amount, "DECLINED", "Insufficient balance", userId);
                transactionRepository.save(txn);
                return new TransactionResponse(false, "Insufficient balance");
            }
            cardService.processWithdrawal(card, amount);
        } else if ("topup".equalsIgnoreCase(type)) {
            cardService.processTopUp(card, amount);
        }

        Transaction txn = new Transaction(cardNumber, type, amount, "APPROVED", null, userId);
        Transaction saved = transactionRepository.save(txn);

        return new TransactionResponse(true, "Transaction approved",
                String.valueOf(saved.getId()), card.getBalance());
    }
}
