package com.tonkar.volleyballreferee.engine.stored.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class ApiNewUser {

    private String id;
    private String pseudo;
    private String email;
    private String password;
    private String purchaseToken;

}
