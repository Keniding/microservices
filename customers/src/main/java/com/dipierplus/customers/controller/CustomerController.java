package com.dipierplus.customers.controller;

import com.dipierplus.customers.dto.BasicCustomerResponse;
import com.dipierplus.customers.dto.CustomerRequest;
import com.dipierplus.customers.dto.CustomerResponse;
import com.dipierplus.customers.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Void> createCustomer(@RequestBody CustomerRequest customerRequest) {
        customerService.createCustomer(customerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "false") boolean fullDetails) {
        List<CustomerResponse> customers = customerService.getAllCustomers(fullDetails);

        if (fullDetails) {
            return ResponseEntity.ok(customers);
        }

        List<BasicCustomerResponse> basicCustomers = customers.stream()
                .map(customer -> new BasicCustomerResponse(
                        customer.getId(),
                        customer.getFirstName(),
                        customer.getLastName(),
                        customer.getPhoneNumber()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(basicCustomers);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean fullDetails) {
        CustomerResponse customer = customerService.getCustomer(id, fullDetails);

        if (fullDetails) {
            return customer != null ? ResponseEntity.ok(customer) : ResponseEntity.notFound().build();
        }

        BasicCustomerResponse basicCustomerResponse = new BasicCustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhoneNumber()
        );

        return ResponseEntity.ok(basicCustomerResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCustomer(
            @PathVariable String id,
            @RequestBody CustomerRequest customerRequest) {
        customerService.updateCustomer(id, customerRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok().build();
    }
}