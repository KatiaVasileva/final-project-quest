package com.vasileva.finalprojectquest.dto;

import com.vasileva.finalprojectquest.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSaveDto {

    @NotBlank(message = "validation.login.empty")
    @Size(min = 3, max = 15, message = "validation.login.size")
    private String login;

    @NotBlank(message = "validation.email.empty")
    @Email(message = "validation.email.invalid")
    private String email;

    @NotBlank(message = "validation.password.empty")
    @Size(min = 4, max = 15, message = "validation.password.size")
    private String password;

    private Role role;
}
