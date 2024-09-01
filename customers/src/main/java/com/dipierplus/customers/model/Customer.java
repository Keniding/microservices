package com.dipierplus.customers.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import java.time.LocalDate;
import java.util.List;

@Document(value = "customer")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Customer {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private List<String> allergies;
    private List<String> chronicConditions;
    private LocalDate lastVisit;

    public Customer(String firstName, String lastName, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.lastVisit = LocalDate.now();
    }

    public void updateLastVisit() {
        this.lastVisit = LocalDate.now();
    }
}