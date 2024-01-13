package com.tonkar.volleyballreferee.engine.api.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiUserToken {
    private ApiUserSummary user;
    private String         token;
    private long           tokenExpiry;
}
