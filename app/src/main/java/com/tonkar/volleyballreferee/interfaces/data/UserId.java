package com.tonkar.volleyballreferee.interfaces.data;

public class UserId {

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
    };

    public static String VBR_USER_ID = "01022018@vbr";

    public static String userIdOf(String socialId, Provider provider) {
        return socialId + '@' + provider.toString();
    }

    public static boolean isGoogle(String userId) {
        return userId.contains(Provider.GOOGLE.toString());
    }

    public static boolean isFacebook(String userId) {
        return userId.contains(Provider.FACEBOOK.toString());
    }
}