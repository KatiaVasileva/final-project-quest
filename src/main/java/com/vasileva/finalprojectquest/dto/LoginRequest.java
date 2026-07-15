package com.vasileva.finalprojectquest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "validation.login.empty")
    private String login;

    @NotBlank(message = "validation.password.empty")
    private String password;
}
