package com.dipierplus.users.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document("users")
@Data
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    private  String password;
    private boolean active;
    private String role;
}
