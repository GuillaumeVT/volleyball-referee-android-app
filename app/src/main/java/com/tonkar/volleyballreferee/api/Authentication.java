package com.tonkar.volleyballreferee.api;

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
    private final String   mUserPseudo;
    private final String   mToken;
    private final Provider mProvider;

    private Authentication(String socialId, Provider provider, String pseudo, String token) {
        mUserId = userIdOf(socialId, provider);
        mUserPseudo = pseudo;
        mToken = token;
        mProvider = provider;
    }

    private Authentication(String userId, String pseudo, String token) {
        mUserId = userId;
        mUserPseudo = pseudo;
        mToken = token;
        mProvider = providerOf(userId);
    }

    public String getUserId() {
        return mUserId;
    }

    public String getUserPseudo() {
        return mUserPseudo;
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

    public static Authentication of(String socialId, Provider provider, String pseudo, String token) {
        return new Authentication(socialId, provider, pseudo, token);
    }

    public static Authentication of(String userId, String pseudo, String token) {
        return new Authentication(userId, pseudo, token);
    }
}
