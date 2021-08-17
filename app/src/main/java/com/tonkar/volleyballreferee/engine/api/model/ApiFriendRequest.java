package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ApiFriendRequest {
    @SerializedName("id")
    private String id;
    @SerializedName("senderId")
    private String senderId;
    @SerializedName("receiverId")
    private String receiverId;
    @SerializedName("senderPseudo")
    private String senderPseudo;
    @SerializedName("receiverPseudo")
    private String receiverPseudo;
}
