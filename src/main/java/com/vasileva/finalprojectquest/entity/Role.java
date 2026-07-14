package com.vasileva.finalprojectquest.entity;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {
    USER, ADMIN, GUEST;

    public SimpleGrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }
}
