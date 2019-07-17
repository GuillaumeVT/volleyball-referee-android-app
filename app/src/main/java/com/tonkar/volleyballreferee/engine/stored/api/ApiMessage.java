package com.tonkar.volleyballreferee.engine.stored.api;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class ApiMessage {

    @SerializedName("id")
    private String id;
    @SerializedName("content")
    private String content;

}
