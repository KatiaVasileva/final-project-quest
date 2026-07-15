package com.vasileva.finalprojectquest.dto;

import com.vasileva.finalprojectquest.entity.Role;
import lombok.Data;

@Data
public class UserSaveDto {
    private String login;
    private String email;
    private String password;
    private Role role;
}
