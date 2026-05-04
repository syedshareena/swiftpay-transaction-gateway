package com.swiftpay.transaction_gateway.service;

import com.swiftpay.transaction_gateway.dto.PaymentRequest;
import com.swiftpay.transaction_gateway.model.Transaction;
import com.swiftpay.transaction_gateway.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public Transaction processPayment(PaymentRequest request) {

        // 1. Idempotency check using Redis
        String redisKey = "txn:" + request.getTransactionId();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new RuntimeException("Duplicate transaction!");
        }
        redisTemplate.opsForValue().set(redisKey, "PROCESSING", 24, TimeUnit.HOURS);

        // 2. Save transaction as PENDING
        Transaction transaction = new Transaction();
        transaction.setSenderId(request.getSenderId());
        transaction.setReceiverId(request.getReceiverId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setStatus("PENDING");
        transaction.setTransactionId(request.getTransactionId());
        transactionRepository.save(transaction);

        // 3. Emit Kafka event with full JSON
        String eventJson = String.format(
                "{\"transactionId\":\"%s\",\"senderId\":\"%s\",\"receiverId\":\"%s\",\"amount\":%s,\"currency\":\"%s\"}",
                transaction.getId(),
                transaction.getSenderId(),
                transaction.getReceiverId(),
                transaction.getAmount(),
                transaction.getCurrency()
        );
        kafkaTemplate.send("payment-initiated", eventJson);
        log.info("Payment initiated: {}", transaction.getId());

        return transaction;
    }
}