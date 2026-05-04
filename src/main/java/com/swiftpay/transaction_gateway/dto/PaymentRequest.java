package com.swiftpay.transaction_gateway.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequest {

    private UUID senderId;
    private UUID receiverId;
    private BigDecimal amount;
    private String currency;
    private String transactionId; // for idempotency
}