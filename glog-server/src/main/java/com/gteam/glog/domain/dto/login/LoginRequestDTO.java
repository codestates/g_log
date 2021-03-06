package com.gteam.glog.domain.dto.login;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Validated
@Builder
public class LoginRequestDTO {
    @NotNull
    @Email
    private String mail;

    @NotNull
    @Size(min=8)
    private String pwd;
}
