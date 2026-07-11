package com.orbitamarket.payments.controller;

import com.orbitamarket.payments.model.Account;
import com.orbitamarket.payments.model.dto.BalanceResponse;
import com.orbitamarket.payments.model.dto.TopUpRequest;
import com.orbitamarket.payments.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestHeader("X-User-Id") String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Account account = accountService.createAccount(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/accounts/top-up")
    public ResponseEntity<BalanceResponse> topUp(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TopUpRequest request) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Account account = accountService.topUp(userId, request.getAmount());
        BalanceResponse response = BalanceResponse.builder()
                .userId(account.getUserId())
                .balance(account.getBalance())
                .currency("geocredits")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/balance")
    public ResponseEntity<BalanceResponse> getBalance(@RequestHeader("X-User-Id") String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Account account = accountService.getAccount(userId);
        BalanceResponse response = BalanceResponse.builder()
                .userId(account.getUserId())
                .balance(account.getBalance())
                .currency("geocredits")
                .build();
        return ResponseEntity.ok(response);
    }
}