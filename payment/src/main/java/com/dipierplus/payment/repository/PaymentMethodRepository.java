package com.dipierplus.payment.repository;

import com.dipierplus.payment.model.PaymentMethod;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends MongoRepository<PaymentMethod, String> {
    List<PaymentMethod> findByCustomerId(String customerId);
}
