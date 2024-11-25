package com.tonkar.volleyballreferee.engine.api.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginCredentialsDto {
    private String pseudo;
    private String password;
}