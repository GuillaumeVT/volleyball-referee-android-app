package com.tonkar.volleyballreferee.engine.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.api.*;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.database.*;
import com.tonkar.volleyballreferee.engine.database.model.FriendEntity;
import com.tonkar.volleyballreferee.engine.worker.SyncWorker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

import okhttp3.*;

public class StoredUserManager implements StoredUserService {

    private final Context       mContext;
    private final VbrRepository mRepository;

    public StoredUserManager(Context context) {
        mContext = context;
        mRepository = new VbrRepository(mContext);
    }

    @Override
    public void getUser(String purchaseToken, AsyncUserRequestListener listener) {
        if (PrefUtils.shouldSignIn(mContext)) {
            VbrApi.getInstance().getUser(purchaseToken, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    if (listener != null) {
                        listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            ApiUserSummary user = JsonConverters.GSON.fromJson(body.string(), ApiUserSummary.class);
                            PrefUtils.storeUser(mContext, user);
                            if (listener != null) {
                                listener.onUserReceived(user);
                            }
                        }
                    } else {
                        Log.e(Tags.STORED_USER,
                              String.format(Locale.getDefault(), "Error %d while getting user from purchase token", response.code()));
                        if (listener != null) {
                            listener.onError(response.code());
                        }
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void createUser(ApiNewUser newUser, AsyncUserRequestListener listener) {
        if (PrefUtils.shouldSignIn(mContext)) {
            VbrApi.getInstance().createUser(newUser, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_CREATED) {
                        try (ResponseBody body = response.body()) {
                            ApiUserToken userToken = JsonConverters.GSON.fromJson(body.string(), ApiUserToken.class);
                            PrefUtils.signIn(mContext, userToken);
                            listener.onUserTokenReceived(userToken);
                        }
                    } else {
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while creating user", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void signInUser(String email, String password, AsyncUserRequestListener listener) {
        if (PrefUtils.shouldSignIn(mContext)) {
            ApiEmailCredentials emailCredentials = new ApiEmailCredentials(email, password);

            VbrApi.getInstance().signInUser(emailCredentials, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            ApiUserToken userToken = JsonConverters.GSON.fromJson(body.string(), ApiUserToken.class);
                            PrefUtils.signIn(mContext, userToken);
                            listener.onUserTokenReceived(userToken);
                            syncAll();
                        }
                    } else {
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while signing in user", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void initiateUserPasswordRecovery(String email, AsyncUserRequestListener listener) {
        if (PrefUtils.shouldSignIn(mContext)) {
            VbrApi.getInstance().initiateUserPasswordRecovery(email, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        listener.onUserPasswordRecoveryInitiated();
                    } else {
                        Log.e(Tags.STORED_USER,
                              String.format(Locale.getDefault(), "Error %d while initiating user password recovery", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void updateUserPassword(ApiUserPasswordUpdate passwordUpdate, AsyncUserRequestListener listener) {
        if (PrefUtils.shouldSignIn(mContext)) {
            VbrApi.getInstance().updateUserPassword(passwordUpdate, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            ApiUserToken userToken = JsonConverters.GSON.fromJson(body.string(), ApiUserToken.class);
                            PrefUtils.signIn(mContext, userToken);
                            listener.onUserTokenReceived(userToken);
                            syncAll();
                        }
                    } else {
                        Log.e(Tags.STORED_USER,
                              String.format(Locale.getDefault(), "Error %d while updating user password", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void syncUser() {
        refreshSubscriptionPurchaseToken();
        downloadFriendsAndRequests(null);
    }

    @Override
    public void downloadFriendsAndRequests(AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().getFriendsAndRequests(mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    if (listener != null) {
                        listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            ApiFriendsAndRequests friendsAndRequests = JsonConverters.GSON.fromJson(body.string(),
                                                                                                    new TypeToken<ApiFriendsAndRequests>() {}.getType());

                            mRepository.insertFriends(friendsAndRequests.getFriends(), false);
                            if (listener != null) {
                                listener.onFriendsAndRequestsReceived(friendsAndRequests);
                            }
                        }
                    } else {
                        Log.e(Tags.STORED_USER,
                              String.format(Locale.getDefault(), "Error %d getting friends and requests", response.code()));
                        if (listener != null) {
                            listener.onError(response.code());
                        }
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
        }
    }

    @Override
    public List<ApiFriend> listReferees() {
        List<ApiFriend> referees = new ArrayList<>();

        if (PrefUtils.canSync(mContext)) {
            ApiUserSummary user = PrefUtils.getUser(mContext);
            referees.add(new ApiFriend(user.getId(), user.getPseudo()));

            for (FriendEntity friendEntity : VbrDatabase.getInstance(mContext).friendDao().listFriends()) {
                referees.add(new ApiFriend(friendEntity.getId(), friendEntity.getPseudo()));
            }
        }

        return referees;
    }

    @Override
    public void sendFriendRequest(String friendPseudo, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().sendFriendRequest(friendPseudo, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED) {
                        listener.onFriendRequestSent(friendPseudo);
                    } else {
                        Log.e(Tags.STORED_USER,
                              String.format(Locale.getDefault(), "Error %d while sending friend request", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void acceptFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().acceptFriendRequest(friendRequest, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED) {
                        mRepository.insertFriend(friendRequest.getSenderId(), friendRequest.getSenderPseudo(), false);
                        listener.onFriendRequestAccepted(friendRequest);
                    } else {
                        Log.e(Tags.STORED_USER,
                              String.format(Locale.getDefault(), "Error %d while accepting friend request", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void rejectFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().rejectFriendRequest(friendRequest, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED) {
                        listener.onFriendRequestRejected(friendRequest);
                    } else {
                        Log.e(Tags.STORED_USER,
                              String.format(Locale.getDefault(), "Error %d while rejecting friend request", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void removeFriend(ApiFriend friend, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().removeFriend(friend, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                        mRepository.removeFriend(friend.getId());
                        listener.onFriendRemoved(friend);
                    } else {
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while removing friend", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    private void refreshSubscriptionPurchaseToken() {
        if (PrefUtils.canSync(mContext) && PrefUtils.isWebPremiumSubscribed(mContext)) {
            VbrApi.getInstance().refreshSubscriptionPurchaseToken(mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != HttpURLConnection.HTTP_OK) {
                        Log.e(Tags.STORED_USER,
                              String.format(Locale.getDefault(), "Error %d while refreshing subscription purchase token", response.code()));
                    }
                }
            });
        }
    }

    private void syncAll() {
        SyncWorker.enqueue(mContext.getApplicationContext());
    }
}
