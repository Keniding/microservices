package com.dipierplus.payment.controller;

import com.dipierplus.payment.model.PaymentMethod;
import com.dipierplus.payment.service.PaymentMethodService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods(@RequestParam String costumerId) {
        List<PaymentMethod> methods = paymentMethodService.getAllMethods(costumerId);
        return ResponseEntity.ok(methods);
    }

    @PostMapping()
    public ResponseEntity<PaymentMethod> addPaymentMethod(@RequestBody PaymentMethod paymentMethod) {
        PaymentMethod createdMethod = paymentMethodService.addPaymentMethod(paymentMethod);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMethod);
    }

    @PutMapping("/{methodId}")
    public ResponseEntity<PaymentMethod> updatePaymentMethod(@PathVariable String methodId, @RequestBody PaymentMethod paymentMethod) {
        PaymentMethod updatedMethod = paymentMethodService.updatePaymentMethod(methodId, paymentMethod);
        return ResponseEntity.ok(updatedMethod);
    }

    @DeleteMapping("/{methodId}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable String methodId) {
        paymentMethodService.deletePaymentMethod(methodId);
        return ResponseEntity.noContent().build();
    }
}
