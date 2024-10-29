package com.dipierplus.invoice.repository;

import com.dipierplus.invoice.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    ArrayList<Invoice> findByCustomerId(String customerId);
}
