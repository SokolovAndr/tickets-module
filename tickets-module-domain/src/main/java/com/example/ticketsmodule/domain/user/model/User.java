package com.example.ticketsmodule.domain.user.model;

import com.example.ticketsmodule.domain.common.model.CommonModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class User extends CommonModel {
    private String login;
    private String password;
    private String fullName;
    private UserRoleEnum role;
}
