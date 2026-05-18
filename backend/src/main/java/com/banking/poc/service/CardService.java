package com.banking.poc.service;

import com.banking.poc.model.Card;
import com.banking.poc.repository.CardRepository;
import com.banking.poc.security.CardEncryptor;
import com.banking.poc.security.PinHasher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Card createCard(String cardNumber, String plainPin, double initialBalance, String cardHolderName, String userId) {
        String pinHash = PinHasher.hashPin(plainPin);
        String encryptedCardNumber = CardEncryptor.encrypt(cardNumber);
        Card card = new Card(encryptedCardNumber, pinHash, initialBalance, cardHolderName, userId);
        return cardRepository.save(card);
    }

    public Optional<Card> findByCardNumber(String cardNumber) {
        String encryptedCardNumber = CardEncryptor.encrypt(cardNumber);
        return cardRepository.findByCardNumber(encryptedCardNumber);
    }

    public String getDecryptedCardNumber(Card card) {
        return CardEncryptor.decrypt(card.getCardNumber());
    }

    public boolean validatePin(Card card, String plainPin) {
        return PinHasher.verifyPin(plainPin, card.getPinHash());
    }

    public boolean hasSufficientBalance(Card card, double amount) {
        return card.getBalance() >= amount;
    }

    public Card processWithdrawal(Card card, double amount) {
        card.setBalance(card.getBalance() - amount);
        return cardRepository.save(card);
    }

    public Card processTopUp(Card card, double amount) {
        card.setBalance(card.getBalance() + amount);
        return cardRepository.save(card);
    }
}
