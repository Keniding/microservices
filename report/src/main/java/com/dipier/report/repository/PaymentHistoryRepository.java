package com.dipier.report.repository;

import com.dipier.report.model.PaymentHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentHistoryRepository extends MongoRepository<PaymentHistory, String> {
}
