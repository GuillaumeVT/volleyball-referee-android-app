package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
import com.tonkar.volleyballreferee.business.data.db.FriendEntity;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.AsyncFriendRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.AsyncUserRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredUserService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoredUser implements StoredUserService {

    private final Context mContext;

    public StoredUser(Context context) {
        mContext = context;
    }

    @Override
    public void createUser(String userId, String pseudo, AsyncUserRequestListener listener) {
        if (PrefUtils.shouldCreateUser(mContext)) {
            ApiUser user = new ApiUser();
            user.setId(userId);
            user.setPseudo(pseudo);
            user.setFriends(new ArrayList<>());
            final String userStr = writeUser(user);

            Request request = ApiUtils.buildPost(ApiUtils.USER_API_URL, userStr);

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED) {
                        PrefUtils.storeUserPseudo(mContext, pseudo);
                        listener.onUserCreated(user);
                    } else {
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d creating user", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void downloadUser(AsyncUserRequestListener listener) {
        if (PrefUtils.canSync(mContext) || PrefUtils.shouldCreateUser(mContext)) {
            Authentication authentication = PrefUtils.getAuthentication(mContext);

            Request request = ApiUtils.buildGet(ApiUtils.USERS_API_URL, authentication);

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
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
                        ApiUser user = readUser(response.body().string());

                        PrefUtils.signIn(mContext, Authentication.of(user.getId(), user.getPseudo(), authentication.getToken()));
                        insertFriendsIntoDb(user.getFriends(), false);

                        if (listener != null) {
                            listener.onUserReceived(user);
                        }
                    } else {
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d getting user", response.code()));
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
    public void syncUser() {
        downloadUser(null);
    }

    @Override
    public void downloadFriendsAndRequests(AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            Request request = ApiUtils.buildGet(ApiUtils.FRIENDS_API_URL, PrefUtils.getAuthentication(mContext));

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        ApiFriendsAndRequests friendsAndRequests = readFriendsAndRequests(response.body().string());

                        insertFriendsIntoDb(friendsAndRequests.getFriends(), false);
                        listener.onFriendsAndRequestsReceived(friendsAndRequests);
                    } else {
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d getting friends and requests", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public boolean hasFriends() {
        return AppDatabase.getInstance(mContext).friendDao().count() > 0;
    }

    @Override
    public List<ApiFriend> listReferees() {
        List<ApiFriend> referees = new ArrayList<>();

        if (PrefUtils.canSync(mContext)) {
            Authentication authentication = PrefUtils.getAuthentication(mContext);
            referees.add(new ApiFriend(authentication.getUserId(), authentication.getUserPseudo()));

            for (FriendEntity friendEntity : AppDatabase.getInstance(mContext).friendDao().listFriends()) {
                referees.add(new ApiFriend(friendEntity.getId(), friendEntity.getPseudo()));
            }
        }

        return referees;
    }

    @Override
    public void sendFriendRequest(String friendPseudo, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            Request request = ApiUtils.buildPost(String.format(ApiUtils.FRIENDS_REQUEST_API_URL, friendPseudo), PrefUtils.getAuthentication(mContext));

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
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
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while sending friend request", response.code()));
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
            Request request = ApiUtils.buildPost(String.format(ApiUtils.FRIENDS_ACCEPT_API_URL, friendRequest.getId()), PrefUtils.getAuthentication(mContext));

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED) {
                        insertFriendIntoDb(friendRequest.getSenderId(), friendRequest.getSenderPseudo(), false);
                        listener.onFriendRequestAccepted(friendRequest);
                    } else {
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while accepting friend request", response.code()));
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
            Request request = ApiUtils.buildPost(String.format(ApiUtils.FRIENDS_REJECT_API_URL, friendRequest.getId()), PrefUtils.getAuthentication(mContext));

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
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
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while rejecting friend request", response.code()));
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
            Request request = ApiUtils.buildDelete(String.format(ApiUtils.FRIENDS_REMOVE_API_URL, friend.getId()), PrefUtils.getAuthentication(mContext));

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                        removeFriendFromDb(friend.getId());
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

    private ApiUser readUser(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.USER_TYPE);
    }

    private String writeUser(ApiUser user) {
        return JsonIOUtils.GSON.toJson(user, JsonIOUtils.USER_TYPE);
    }

    private ApiFriendsAndRequests readFriendsAndRequests(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.FRIENDS_AND_REQUESTS_TYPE);
    }

    private void insertFriendIntoDb(final String friendId, final String friendPseudo, boolean syncInsertion) {
        Runnable runnable = () -> {
            FriendEntity friendEntity = new FriendEntity();
            friendEntity.setId(friendId);
            friendEntity.setPseudo(friendPseudo);
            AppDatabase.getInstance(mContext).friendDao().insert(friendEntity);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            new Thread(runnable).start();
        }
    }

    private void removeFriendFromDb(final String friendId) {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).friendDao().deleteById(friendId);
            }
        }.start();
    }

    private void insertFriendsIntoDb(final List<ApiFriend> friends, boolean syncInsertion) {
        Runnable runnable = () -> {
            AppDatabase.getInstance(mContext).friendDao().deleteAll();
            for (ApiFriend friend : friends) {
                FriendEntity friendEntity = new FriendEntity();
                friendEntity.setId(friend.getId());
                friendEntity.setPseudo(friend.getPseudo());
                AppDatabase.getInstance(mContext).friendDao().insert(friendEntity);
            }
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            new Thread(runnable).start();
        }
    }
}
