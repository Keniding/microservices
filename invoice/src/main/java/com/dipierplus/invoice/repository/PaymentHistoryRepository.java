package com.dipierplus.invoice.repository;

import com.dipierplus.invoice.model.PaymentHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentHistoryRepository extends MongoRepository<PaymentHistory, String> {
}
