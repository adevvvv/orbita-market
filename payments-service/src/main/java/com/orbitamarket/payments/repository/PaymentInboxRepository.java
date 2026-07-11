package com.orbitamarket.payments.repository;

import com.orbitamarket.payments.model.PaymentInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentInboxRepository extends JpaRepository<PaymentInbox, UUID> {
    Optional<PaymentInbox> findByOrderId(UUID orderId);
    boolean existsByEventId(UUID eventId);
}