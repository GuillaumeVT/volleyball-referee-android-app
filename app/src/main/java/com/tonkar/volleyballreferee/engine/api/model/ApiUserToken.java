package com.tonkar.volleyballreferee.engine.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiUserToken {
    private ApiUserSummary user;
    private String         token;
    private long           tokenExpiry;
}
