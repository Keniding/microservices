package com.dipierplus.invoice.controller;

import com.dipierplus.invoice.impl.BillingServiceImp;
import com.dipierplus.invoice.model.Invoice;
import com.dipierplus.invoice.service.BillingService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(BillingController.class);

    @GetMapping("/invoices/{customerId}")
    public ResponseEntity<ArrayList<Invoice>> getAllInvoices(@PathVariable String customerId) {
        ArrayList<Invoice> invoices = billingService.getAllInvoices(customerId);
        return new ResponseEntity<>(invoices, HttpStatus.OK);
    }

    @PostMapping("/invoice")
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        logger.info("Recibiendo request para crear factura: {}", invoice);

        if (invoice == null || invoice.getCustomerId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Invoice createdInvoice = billingService.createInvoice(invoice);
            logger.info("Factura creada exitosamente: {}", createdInvoice);

            if (createdInvoice.getId() == null || createdInvoice.getId().isEmpty()) {
                logger.error("Factura creada sin ID");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error al crear factura", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
