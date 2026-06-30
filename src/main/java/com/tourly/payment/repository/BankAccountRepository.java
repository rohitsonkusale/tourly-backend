package com.tourly.payment.repository;

import com.tourly.payment.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<BankAccount> findByUserIdAndIsPrimaryTrue(Long userId);

    Optional<BankAccount> findByIdAndUserId(Long id, Long userId);
}
