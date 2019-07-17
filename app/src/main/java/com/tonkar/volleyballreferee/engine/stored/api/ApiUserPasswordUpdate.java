package com.tonkar.volleyballreferee.engine.stored.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class ApiUserPasswordUpdate {

    private String currentPassword;
    private String newPassword;

}
