package com.dipierplus.downloads.repository;

import com.dipierplus.downloads.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    ArrayList<Invoice> findByCustomerId(String customerId);
}
