package com.dipierplus.users.security;

import lombok.Data;

@Data
public class AuthCredentials {
    private String username;
    private String password;
}
