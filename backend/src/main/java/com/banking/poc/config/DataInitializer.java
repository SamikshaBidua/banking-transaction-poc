package com.banking.poc.config;

import com.banking.poc.model.Card;
import com.banking.poc.model.User;
import com.banking.poc.repository.CardRepository;
import com.banking.poc.repository.UserRepository;
import com.banking.poc.security.CardEncryptor;
import com.banking.poc.security.PinHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public DataInitializer(UserRepository userRepository, CardRepository cardRepository) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    public void run(String... args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Create Super Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(new User("admin", encoder.encode("admin123"), "ADMIN"));
        }

        // Create Customer users
        if (userRepository.findByUsername("john").isEmpty()) {
            userRepository.save(new User("john", encoder.encode("john123"), "CUSTOMER"));
        }
        if (userRepository.findByUsername("jane").isEmpty()) {
            userRepository.save(new User("jane", encoder.encode("jane123"), "CUSTOMER"));
        }

        // Create sample cards (Visa cards starting with 4)
        if (cardRepository.count() == 0) {
            createCard("4111111111111111", "1234", 5000.00, "John Doe", "john");
            createCard("4222222222222222", "5678", 3000.00, "Jane Smith", "jane");
            createCard("4333333333333333", "9999", 10000.00, "John Doe", "john");

            // Non-Visa card (for testing "Card range not supported")
            createCard("5111111111111111", "1111", 2000.00, "Test User", "john");
        }
    }

    private void createCard(String cardNumber, String pin, double balance, String holderName, String userId) {
        String encryptedCardNumber = CardEncryptor.encrypt(cardNumber);
        String pinHash = PinHasher.hashPin(pin);
        cardRepository.save(new Card(encryptedCardNumber, pinHash, balance, holderName, userId));
    }
}
