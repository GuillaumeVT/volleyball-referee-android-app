package com.tonkar.volleyballreferee.engine.api.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ApiUserSummary {

    public static String VBR_USER_ID = "01022018@vbr";
    public static String VBR_PSEUDO  = "VBR";
    public static String VBR_EMAIL   = "vbr.app.team@gmail.com";

    private String id;
    private String pseudo;
    private String email;

    public static ApiUserSummary emptyUser() {
        return new ApiUserSummary(VBR_USER_ID, VBR_PSEUDO, VBR_EMAIL);
    }

}
