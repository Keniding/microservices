package com.dipierplus.gateway.model;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
