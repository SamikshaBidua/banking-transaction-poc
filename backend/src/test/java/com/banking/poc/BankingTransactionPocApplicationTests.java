package com.banking.poc;

import com.banking.poc.dto.TransactionRequest;
import com.banking.poc.dto.TransactionResponse;
import com.banking.poc.model.Card;
import com.banking.poc.model.Transaction;
import com.banking.poc.model.User;
import com.banking.poc.repository.CardRepository;
import com.banking.poc.repository.TransactionRepository;
import com.banking.poc.repository.UserRepository;
import com.banking.poc.security.CardEncryptor;
import com.banking.poc.security.PinHasher;
import com.banking.poc.service.AuthService;
import com.banking.poc.service.System1Service;
import com.banking.poc.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BankingTransactionPocApplicationTests {

    @Autowired
    private System1Service system1Service;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        userRepository.save(new User("admin", encoder.encode("admin123"), "ADMIN"));
        userRepository.save(new User("john", encoder.encode("john123"), "CUSTOMER"));

        // Create test cards
        String encryptedVisa = CardEncryptor.encrypt("4111111111111111");
        String pinHash = PinHasher.hashPin("1234");
        cardRepository.save(new Card(encryptedVisa, pinHash, 5000.00, "John Doe", "john"));

        String encryptedVisa2 = CardEncryptor.encrypt("4222222222222222");
        String pinHash2 = PinHasher.hashPin("5678");
        cardRepository.save(new Card(encryptedVisa2, pinHash2, 3000.00, "Jane Smith", "jane"));

        String encryptedNonVisa = CardEncryptor.encrypt("5111111111111111");
        String pinHash3 = PinHasher.hashPin("1111");
        cardRepository.save(new Card(encryptedNonVisa, pinHash3, 2000.00, "Test User", "john"));
    }

    // === System 1: Validation Tests ===

    @Test
    void testMissingCardNumber() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber(null);
        request.setPin("1234");
        request.setAmount(100.0);
        request.setType("withdraw");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertFalse(response.isSuccess());
        assertEquals("Card number is required", response.getMessage());
    }

    @Test
    void testMissingPin() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setPin(null);
        request.setAmount(100.0);
        request.setType("withdraw");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertFalse(response.isSuccess());
        assertEquals("PIN is required", response.getMessage());
    }

    @Test
    void testNegativeAmount() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setPin("1234");
        request.setAmount(-50.0);
        request.setType("withdraw");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertFalse(response.isSuccess());
        assertEquals("Amount must be positive", response.getMessage());
    }

    @Test
    void testZeroAmount() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setPin("1234");
        request.setAmount(0.0);
        request.setType("withdraw");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertFalse(response.isSuccess());
        assertEquals("Amount must be positive", response.getMessage());
    }

    @Test
    void testInvalidType() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setPin("1234");
        request.setAmount(100.0);
        request.setType("transfer");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertFalse(response.isSuccess());
        assertEquals("Type must be 'withdraw' or 'topup'", response.getMessage());
    }

    // === System 1: Routing Tests ===

    @Test
    void testNonVisaCardDeclined() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("5111111111111111");
        request.setPin("1111");
        request.setAmount(100.0);
        request.setType("withdraw");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertFalse(response.isSuccess());
        assertEquals("Card range not supported", response.getMessage());
    }

    @Test
    void testVisaCardAccepted() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setPin("1234");
        request.setAmount(100.0);
        request.setType("withdraw");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertTrue(response.isSuccess());
        assertEquals("Transaction approved", response.getMessage());
    }

    // === System 2: Card Validation Tests ===

    @Test
    void testInvalidCardNumber() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4999999999999999");
        request.setPin("1234");
        request.setAmount(100.0);
        request.setType("withdraw");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertFalse(response.isSuccess());
        assertEquals("Invalid card", response.getMessage());
    }

    @Test
    void testInvalidPin() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setPin("0000");
        request.setAmount(100.0);
        request.setType("withdraw");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertFalse(response.isSuccess());
        assertEquals("Invalid PIN", response.getMessage());
    }

    // === System 2: Balance Tests ===

    @Test
    void testSuccessfulWithdrawal() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setPin("1234");
        request.setAmount(500.0);
        request.setType("withdraw");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertTrue(response.isSuccess());
        assertEquals(4500.0, response.getNewBalance(), 0.01);
    }

    @Test
    void testInsufficientBalance() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setPin("1234");
        request.setAmount(10000.0);
        request.setType("withdraw");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertFalse(response.isSuccess());
        assertEquals("Insufficient balance", response.getMessage());
    }

    @Test
    void testSuccessfulTopUp() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setPin("1234");
        request.setAmount(1000.0);
        request.setType("topup");

        TransactionResponse response = system1Service.handleTransaction(request, "john");
        assertTrue(response.isSuccess());
        assertEquals(6000.0, response.getNewBalance(), 0.01);
    }

    // === PIN Security Tests ===

    @Test
    void testPinHashing() {
        String pin = "1234";
        String hash1 = PinHasher.hashPin(pin);
        String hash2 = PinHasher.hashPin(pin);
        assertEquals(hash1, hash2, "Same PIN should produce same hash");
        assertNotEquals(pin, hash1, "Hash should not equal plain PIN");
    }

    @Test
    void testPinVerification() {
        String pin = "1234";
        String hash = PinHasher.hashPin(pin);
        assertTrue(PinHasher.verifyPin(pin, hash));
        assertFalse(PinHasher.verifyPin("0000", hash));
    }

    // === Card Encryption Tests ===

    @Test
    void testCardEncryption() {
        String cardNumber = "4111111111111111";
        String encrypted = CardEncryptor.encrypt(cardNumber);
        assertNotEquals(cardNumber, encrypted, "Encrypted card should differ from plain");
        String decrypted = CardEncryptor.decrypt(encrypted);
        assertEquals(cardNumber, decrypted, "Decrypted card should match original");
    }

    // === Transaction History Tests ===

    @Test
    void testCustomerSeesOwnTransactions() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setPin("1234");
        request.setAmount(100.0);
        request.setType("topup");
        system1Service.handleTransaction(request, "john");

        List<Transaction> johnTxns = transactionService.getTransactionsByUserId("john");
        assertFalse(johnTxns.isEmpty());
        assertTrue(johnTxns.stream().allMatch(t -> t.getUserId().equals("john")));
    }

    @Test
    void testAdminSeesAllTransactions() {
        TransactionRequest req1 = new TransactionRequest();
        req1.setCardNumber("4111111111111111");
        req1.setPin("1234");
        req1.setAmount(100.0);
        req1.setType("topup");
        system1Service.handleTransaction(req1, "john");

        TransactionRequest req2 = new TransactionRequest();
        req2.setCardNumber("4222222222222222");
        req2.setPin("5678");
        req2.setAmount(200.0);
        req2.setType("topup");
        system1Service.handleTransaction(req2, "jane");

        List<Transaction> allTxns = transactionService.getAllTransactions();
        assertEquals(2, allTxns.size());
    }

    // === Auth Tests ===

    @Test
    void testSuccessfulLogin() {
        assertDoesNotThrow(() -> authService.login(new com.banking.poc.dto.LoginRequest("admin", "admin123")));
    }

    @Test
    void testFailedLogin() {
        assertThrows(RuntimeException.class, () ->
            authService.login(new com.banking.poc.dto.LoginRequest("admin", "wrongpassword")));
    }
}
