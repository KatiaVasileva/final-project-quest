package com.vasileva.finalprojectquest.mapper;

import com.vasileva.finalprojectquest.dto.UserAdminDto;
import com.vasileva.finalprojectquest.dto.UserSaveDto;
import com.vasileva.finalprojectquest.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserAdminMapper {

    UserAdminDto toDto(User user);

    User toEntity(UserSaveDto dto);

    List<UserAdminDto> toDtoList(List<User> users);
}
