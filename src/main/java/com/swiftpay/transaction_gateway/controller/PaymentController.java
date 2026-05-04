package com.swiftpay.transaction_gateway.controller;

import com.swiftpay.transaction_gateway.dto.PaymentRequest;
import com.swiftpay.transaction_gateway.model.Transaction;
import com.swiftpay.transaction_gateway.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Gateway", description = "APIs for initiating payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Initiate a payment")
    public ResponseEntity<?> initiatePayment(@RequestBody PaymentRequest request) {
        try {
            Transaction txn = paymentService.processPayment(request);
            return ResponseEntity.accepted().body(txn);
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(
                    java.util.Map.of("error", e.getMessage())
            );
        }
    }
}