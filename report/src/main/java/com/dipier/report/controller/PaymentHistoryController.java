package com.dipier.report.controller;

import com.dipier.report.model.PaymentHistory;
import com.dipier.report.service.PaymentHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-history")
@AllArgsConstructor
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    @GetMapping
    public ResponseEntity<List<PaymentHistory>> getAllPaymentHistory() {
        return ResponseEntity.ok(paymentHistoryService.getAllPaymentHistory());
    }

    @PostMapping
    public ResponseEntity<PaymentHistory> createPaymentHistory(@RequestBody PaymentHistory paymentHistory) {
        PaymentHistory createdPayment = paymentHistoryService.createPaymentHistory(paymentHistory);
        return new ResponseEntity<>(createdPayment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentHistory> getPaymentHistory(@PathVariable String id) {
        return ResponseEntity.ok(paymentHistoryService.getPaymentHistory(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentHistory> updatePaymentHistory(
            @PathVariable String id,
            @RequestBody PaymentHistory paymentHistory) {
        paymentHistory.setId(id);
        return ResponseEntity.ok(paymentHistoryService.updatePaymentHistory(paymentHistory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentHistory(@PathVariable String id) {
        paymentHistoryService.deletePaymentHistory(id);
        return ResponseEntity.noContent().build();
    }
}
