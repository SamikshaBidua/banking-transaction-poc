package com.banking.poc.controller;

import com.banking.poc.model.Card;
import com.banking.poc.repository.CardRepository;
import com.banking.poc.security.CardEncryptor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardRepository cardRepository;

    public CardController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @GetMapping("/balance")
    public ResponseEntity<List<Map<String, Object>>> getCardBalances(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Card> cards = cardRepository.findAll().stream()
                .filter(c -> c.getUserId().equals(userDetails.getUsername()))
                .toList();

        List<Map<String, Object>> result = cards.stream().map(card -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", card.getId());
            String decrypted = CardEncryptor.decrypt(card.getCardNumber());
            m.put("cardNumber", "****-****-****-" + decrypted.substring(decrypted.length() - 4));
            m.put("balance", card.getBalance());
            m.put("cardHolderName", card.getCardHolderName());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }
}
