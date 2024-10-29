package com.dipierplus.invoice.controller;

import com.dipierplus.invoice.model.Invoice;
import com.dipierplus.invoice.service.BillingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/billing")
@AllArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/invoices/{customerId}")
    public ResponseEntity<ArrayList<Invoice>> getAllInvoices(@PathVariable String customerId) {
        ArrayList<Invoice> invoices = billingService.getAllInvoices(customerId);
        return new ResponseEntity<>(invoices, HttpStatus.OK);
    }

    @PostMapping("/invoice")
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        Invoice createdInvoice = billingService.createInvoice(invoice);
        return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    }

    @GetMapping("/invoice/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable String id) {
        try {
            Invoice invoice = billingService.getInvoice(id);
            return new ResponseEntity<>(invoice, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/invoice")
    public ResponseEntity<Invoice> updateInvoice(@RequestBody Invoice invoice) {
        try {
            Invoice updatedInvoice = billingService.updateInvoice(invoice);
            return new ResponseEntity<>(updatedInvoice, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/invoice/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable String id) {
        try {
            billingService.deleteInvoice(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/process-invoice")
    public ResponseEntity<String> processInvoice(@RequestParam String customerId) {
        try {
            billingService.calculateAndProcessInvoice(customerId);
            return ResponseEntity.ok("Invoice processing initiated for customer: " +customerId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error initiating invoice processing: " + e.getMessage());
        }
    }
}
