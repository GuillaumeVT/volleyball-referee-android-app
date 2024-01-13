package com.tonkar.volleyballreferee.engine.api.model;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ApiNewUser {
    private String id;
    private String pseudo;
    private String email;
    private String password;
    private String purchaseToken;
}
