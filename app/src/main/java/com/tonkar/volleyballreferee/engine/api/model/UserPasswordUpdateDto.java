package com.tonkar.volleyballreferee.engine.api.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserPasswordUpdateDto {
    private String currentPassword;
    private String newPassword;
}
