package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.*;

import lombok.*;

@Getter
@Setter
public class PageDto<T> {
    @SerializedName("content")
    private List<T> content;
    @SerializedName("empty")
    private boolean empty;
    @SerializedName("first")
    private boolean first;
    @SerializedName("last")
    private boolean last;
    @SerializedName("number")
    private int     number;
    @SerializedName("numberOfElements")
    private int     numberOfElements;
    @SerializedName("size")
    private int     size;
    @SerializedName("totalElements")
    private long    totalElements;
    @SerializedName("totalPages")
    private int     totalPages;

    public PageDto() {
        this.content = new ArrayList<>();
    }
}
