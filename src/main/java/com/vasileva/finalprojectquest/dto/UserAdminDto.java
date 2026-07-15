package com.vasileva.finalprojectquest.dto;

import com.vasileva.finalprojectquest.entity.Role;
import lombok.Data;

@Data
public class UserAdminDto {
    private Long id;
    private String login;
    private String email;
    private Role role;
}
