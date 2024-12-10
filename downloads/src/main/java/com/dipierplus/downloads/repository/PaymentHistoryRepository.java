package com.dipierplus.downloads.repository;

import com.dipierplus.downloads.model.PaymentHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentHistoryRepository extends MongoRepository<PaymentHistory, String> {
    Optional<PaymentHistory> findByInvoiceId(String customerId);
}
