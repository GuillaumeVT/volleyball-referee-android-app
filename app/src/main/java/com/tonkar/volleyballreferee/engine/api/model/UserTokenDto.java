package com.tonkar.volleyballreferee.engine.api.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserTokenDto {
    private UserSummaryDto user;
    private String         token;
    private long           tokenExpiry;
}
