package com.swiftpay.transaction_gateway.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID senderId;

    @Column(nullable = false)
    private UUID receiverId;

    @Column(nullable = false)
    private BigDecimal amount;

    private String currency;

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED

    @Column(unique = true)
    private String transactionId; // add this field

    private LocalDateTime createdAt;


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}