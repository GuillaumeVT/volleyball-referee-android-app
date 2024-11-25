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
    public void signInUser(String pseudo, String password, AsyncUserRequestListener listener) {
        LoginCredentialsDto loginCredentials = new LoginCredentialsDto(pseudo, password);

        VbrApi.getInstance(mContext).signInUser(loginCredentials, mContext, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    try (ResponseBody body = response.body()) {
                        UserTokenDto userToken = JsonConverters.GSON.fromJson(body.string(), UserTokenDto.class);
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
    }

    @Override
    public void updateUserPassword(UserPasswordUpdateDto passwordUpdate, AsyncUserRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).updateUserPassword(passwordUpdate, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            UserTokenDto userToken = JsonConverters.GSON.fromJson(body.string(), UserTokenDto.class);
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
        downloadFriendsAndRequests(null);
    }

    @Override
    public void downloadFriendsAndRequests(AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).getFriendsAndRequests(mContext, new Callback() {
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
                            FriendsAndRequestsDto friendsAndRequests = JsonConverters.GSON.fromJson(body.string(),
                                                                                                    new TypeToken<FriendsAndRequestsDto>() {}.getType());

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
    public List<FriendDto> listReferees() {
        List<FriendDto> referees = new ArrayList<>();

        if (PrefUtils.canSync(mContext)) {
            UserSummaryDto user = PrefUtils.getUser(mContext);
            referees.add(new FriendDto(user.getId(), user.getPseudo()));

            for (FriendEntity friendEntity : VbrDatabase.getInstance(mContext).friendDao().listFriends()) {
                referees.add(new FriendDto(friendEntity.getId(), friendEntity.getPseudo()));
            }
        }

        return referees;
    }

    @Override
    public void sendFriendRequest(String friendPseudo, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).sendFriendRequest(friendPseudo, mContext, new Callback() {
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
    public void acceptFriendRequest(FriendRequestDto friendRequest, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).acceptFriendRequest(friendRequest, mContext, new Callback() {
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
    public void rejectFriendRequest(FriendRequestDto friendRequest, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).rejectFriendRequest(friendRequest, mContext, new Callback() {
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
    public void removeFriend(FriendDto friend, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).removeFriend(friend, mContext, new Callback() {
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

    private void syncAll() {
        SyncWorker.enqueue(mContext.getApplicationContext());
    }
}
