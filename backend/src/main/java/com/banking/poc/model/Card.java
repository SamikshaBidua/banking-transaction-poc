package com.banking.poc.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private String pinHash;

    @Column(nullable = false)
    private double balance;

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private String userId;

    public Card() {}

    public Card(String cardNumber, String pinHash, double balance, String cardHolderName, String userId) {
        this.cardNumber = cardNumber;
        this.pinHash = pinHash;
        this.balance = balance;
        this.cardHolderName = cardHolderName;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
