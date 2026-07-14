package com.vasileva.finalprojectquest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String login;
    private String email;
    private String password;
}
