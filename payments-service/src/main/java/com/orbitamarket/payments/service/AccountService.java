package com.orbitamarket.payments.service;

import com.orbitamarket.payments.exception.*;
import com.orbitamarket.payments.model.Account;
import com.orbitamarket.payments.model.PaymentInbox;
import com.orbitamarket.payments.repository.AccountRepository;
import com.orbitamarket.payments.repository.PaymentInboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PaymentInboxRepository paymentInboxRepository;

    @Transactional
    public Account createAccount(String userId) {
        if (accountRepository.existsByUserId(userId)) {
            throw new AccountAlreadyExistsException("Account already exists for user: " + userId);
        }

        Account account = new Account();
        account.setUserId(userId);
        account.setBalance(0);

        Account saved = accountRepository.save(account);
        log.info("Created account for user: {}", userId);
        return saved;
    }

    @Transactional
    public Account topUp(String userId, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for user: " + userId));

        account.setBalance(account.getBalance() + amount);
        Account updated = accountRepository.save(account);
        log.info("Topped up {} geocredits for user: {}. New balance: {}", amount, userId, updated.getBalance());
        return updated;
    }

    public Account getAccount(String userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for user: " + userId));
    }

    @Transactional
    public void processPayment(UUID orderId, UUID eventId, String userId, Integer amount) {
        // Check idempotency via inbox
        if (paymentInboxRepository.existsByEventId(eventId)) {
            log.info("Duplicate event received for order: {}, event: {}. Skipping.", orderId, eventId);
            return;
        }

        // Record in inbox
        PaymentInbox inbox = new PaymentInbox();
        inbox.setOrderId(orderId);
        inbox.setEventId(eventId);
        inbox.setUserId(userId);
        inbox.setAmount(amount);

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    inbox.setStatus("FAILED");
                    paymentInboxRepository.save(inbox);
                    return new AccountNotFoundException("Account not found for user: " + userId);
                });

        if (account.getBalance() < amount) {
            inbox.setStatus("FAILED");
            paymentInboxRepository.save(inbox);
            log.warn("Insufficient balance for user: {}. Required: {}, Available: {}",
                    userId, amount, account.getBalance());
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // Deduct balance with optimistic locking
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        inbox.setStatus("PROCESSED");
        paymentInboxRepository.save(inbox);

        log.info("Successfully processed payment for order: {}. User: {}, Amount: {}, New balance: {}",
                orderId, userId, amount, account.getBalance());
    }
}