package com.tonkar.volleyballreferee.business.web;

public class Authentication {

    public static String VBR_USER_ID = "01022018@vbr";

    public enum Provider {
        GOOGLE, FACEBOOK;

        @Override
        public String toString() {
            switch (this) {
                case GOOGLE:
                    return "google";
                case FACEBOOK:
                    return "facebook";
                default:
                    return "";
            }
        }
    }

    private final String   mUserId;
    private final String   mToken;
    private final Provider mProvider;

    private Authentication(String socialId, Provider provider, String token) {
        mUserId = userIdOf(socialId, provider);
        mToken = token;
        mProvider = provider;
    }

    private Authentication(String userId, String token) {
        mUserId = userId;
        mToken = token;
        mProvider = providerOf(userId);
    }

    public String getUserId() {
        return mUserId;
    }

    public String getToken() {
        return mToken;
    }

    public Provider getProvider() {
        return mProvider;
    }

    private String userIdOf(String socialId, Provider provider) {
        return socialId + '@' + provider.toString();
    }

    private Provider providerOf(String userId) {
        Provider provider = null;

        if (isFacebook(userId)) {
            provider = Provider.FACEBOOK;
        } else if (isGoogle(userId)) {
            provider = Provider.GOOGLE;
        }

        return provider;
    }

    private boolean isGoogle(String userId) {
        return userId.contains(Provider.GOOGLE.toString());
    }

    private boolean isFacebook(String userId) {
        return userId.contains(Provider.FACEBOOK.toString());
    }

    public static Authentication of(String socialId, Provider provider, String token) {
        return new Authentication(socialId, provider, token);
    }

    public static Authentication of(String userId, String token) {
        return new Authentication(userId, token);
    }
}
