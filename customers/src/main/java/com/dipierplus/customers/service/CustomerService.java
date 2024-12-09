package com.dipierplus.customers.service;

import com.dipierplus.customers.dto.CustomerRequest;
import com.dipierplus.customers.dto.CustomerResponse;
import com.dipierplus.customers.model.Customer;
import com.dipierplus.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    public void createCustomer(CustomerRequest customerRequest) {
        Customer customer = mapToCustomer(customerRequest);
        customer.setLastVisit(LocalDate.now());
        customerRepository.save(customer);
        log.info("Customer {} is saved", customer.getId());
    }

    public List<CustomerResponse> getAllCustomers(boolean fullDetails) {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(customer -> fullDetails ? mapToFullCustomerResponse(customer) : mapToBasicCustomerResponse(customer))
                .toList();
    }

    public CustomerResponse getCustomer(String id, boolean fullDetails) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            log.info("Customer not found {}", id);
            return null;
        }
        Customer customer = customerOpt.get();
        return fullDetails ? mapToFullCustomerResponse(customer) : mapToBasicCustomerResponse(customer);
    }

    public void updateCustomer(String id, CustomerRequest customerRequest) {
        Optional<Customer> existingCustomerOpt = customerRepository.findById(id);
        if (existingCustomerOpt.isEmpty()) {
            log.error("Error updating customer, customer not found {}", id);
            return;
        }

        try {
            Customer existingCustomer = getExistingCustomer(customerRequest, existingCustomerOpt);

            customerRepository.save(existingCustomer);
            log.info("Customer {} updated successfully", id);
        } catch (Exception e) {
            log.error("Error updating customer {}", id, e);
        }
    }

    private static Customer getExistingCustomer(CustomerRequest customerRequest, Optional<Customer> existingCustomerOpt) {
        Customer existingCustomer = existingCustomerOpt.get();
        existingCustomer.setFirstName(customerRequest.getFirstName());
        existingCustomer.setLastName(customerRequest.getLastName());
        existingCustomer.setEmail(customerRequest.getEmail());
        existingCustomer.setPhoneNumber(customerRequest.getPhoneNumber());
        existingCustomer.setDateOfBirth(customerRequest.getDateOfBirth());
        existingCustomer.setAddress(customerRequest.getAddress());
        existingCustomer.setAllergies(customerRequest.getAllergies());
        existingCustomer.setChronicConditions(customerRequest.getChronicConditions());
        existingCustomer.updateLastVisit();
        return existingCustomer;
    }

    public void deleteCustomer(String id) {
        if (getCustomer(id, false) == null) {
            log.error("Error deleting customer, customer not found {}", id);
            return;
        }

        try {
            customerRepository.deleteById(id);
            log.info("Customer {} deleted successfully", id);
        } catch (Exception e) {
            log.error("Error deleting customer {}", id, e);
        }
    }

    private CustomerResponse mapToBasicCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhoneNumber()
        );
    }

    private CustomerResponse mapToFullCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .dateOfBirth(customer.getDateOfBirth())
                .address(customer.getAddress())
                .allergies(customer.getAllergies())
                .chronicConditions(customer.getChronicConditions())
                .lastVisit(customer.getLastVisit())
                .build();
    }

    private Customer mapToCustomer(CustomerRequest request) {
        return Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .allergies(request.getAllergies())
                .chronicConditions(request.getChronicConditions())
                .build();
    }
}