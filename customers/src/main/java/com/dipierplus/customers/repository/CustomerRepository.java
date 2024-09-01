package com.dipierplus.customers.repository;

import com.dipierplus.customers.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {
}
