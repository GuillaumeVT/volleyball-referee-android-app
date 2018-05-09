package com.tonkar.volleyballreferee.interfaces.data;

import com.google.gson.annotations.SerializedName;

public class UserId {

    @SerializedName("socialId")
    private String mSocialId;
    @SerializedName("provider")
    private String mProvider;

    public UserId() {}

    public UserId(String socialId, String provider) {
        mSocialId = socialId;
        mProvider = provider;
    }

    public String getSocialId() {
        return mSocialId;
    }

    public String getProvider() {
        return mProvider;
    }

    public static UserId VBR_USER_ID = new UserId("01022018", "VBR");

    @Override
    public String toString() {
        return mSocialId + '@' + mProvider;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof UserId) {
            UserId other = (UserId) obj;
            result = this.getSocialId().equals(other.getSocialId()) && this.getProvider().equals(other.getProvider());
        }

        return result;
    }
}