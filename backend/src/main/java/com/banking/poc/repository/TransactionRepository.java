package com.banking.poc.repository;

import com.banking.poc.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByTimestampDesc(String userId);
    List<Transaction> findAllByOrderByTimestampDesc();
}
