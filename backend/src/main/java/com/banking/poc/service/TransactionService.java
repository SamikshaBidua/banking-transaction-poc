package com.banking.poc.service;

import com.banking.poc.model.Card;
import com.banking.poc.model.Transaction;
import com.banking.poc.repository.CardRepository;
import com.banking.poc.repository.TransactionRepository;
import com.banking.poc.security.CardEncryptor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public TransactionService(TransactionRepository transactionRepository, CardRepository cardRepository) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> txns = transactionRepository.findAllByOrderByTimestampDesc();
        txns.forEach(this::decryptCardNumber);
        return txns;
    }

    public List<Transaction> getTransactionsByUserId(String userId) {
        List<Transaction> txns = transactionRepository.findByUserIdOrderByTimestampDesc(userId);
        txns.forEach(this::decryptCardNumber);
        return txns;
    }

    private void decryptCardNumber(Transaction txn) {
        try {
            String decrypted = CardEncryptor.decrypt(txn.getCardNumber());
            String masked = "****-****-****-" + decrypted.substring(decrypted.length() - 4);
            txn.setCardNumber(masked);
        } catch (Exception e) {
            txn.setCardNumber("****-****-****-****");
        }
    }
}
