package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ApiMessage {
    @SerializedName("id")
    private String id;
    @SerializedName("content")
    private String content;

}
