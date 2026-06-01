package com.minimarket.dto.auth;

import java.util.Set;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private String username;
    private Set<String> roles;

    public AuthResponse(String token, String username, Set<String> roles) {
        this.token = token;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
