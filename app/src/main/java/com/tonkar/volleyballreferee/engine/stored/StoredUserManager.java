package com.tonkar.volleyballreferee.engine.stored;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.reflect.TypeToken;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.stored.api.*;
import com.tonkar.volleyballreferee.engine.stored.database.AppDatabase;
import com.tonkar.volleyballreferee.engine.stored.database.FriendEntity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoredUserManager implements StoredUserService {

    private final Context mContext;

    public StoredUserManager(Context context) {
        mContext = context;
    }

    @Override
    public void getUser(String purchaseToken, AsyncUserRequestListener listener) {
        if (PrefUtils.shouldSignIn(mContext)) {
            Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/public/users/%s", ApiUtils.BASE_URL, purchaseToken));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
                        ApiUserSummary user = JsonIOUtils.GSON.fromJson(response.body().string(), ApiUserSummary.class);
                        PrefUtils.storeUser(mContext, user);
                        if (listener != null) {
                            listener.onUserReceived(user);
                        }
                    } else {
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while getting user from purchase token", response.code()));
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
            final String newUserStr = JsonIOUtils.GSON.toJson(newUser);

            Request request = ApiUtils.buildPost(String.format(Locale.US, "%s/public/users", ApiUtils.BASE_URL), newUserStr);

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_CREATED) {
                        ApiUserToken userToken = JsonIOUtils.GSON.fromJson(response.body().string(), ApiUserToken.class);
                        PrefUtils.signIn(mContext, userToken);
                        listener.onUserTokenReceived(userToken);
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
            String emailCredentialsStr = JsonIOUtils.GSON.toJson(emailCredentials);

            Request request = ApiUtils.buildPost(String.format("%s/public/users/token", ApiUtils.BASE_URL), emailCredentialsStr);

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        ApiUserToken userToken = JsonIOUtils.GSON.fromJson(response.body().string(), ApiUserToken.class);
                        PrefUtils.signIn(mContext, userToken);
                        listener.onUserTokenReceived(userToken);
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
    public void initiatedUserPasswordRecovery(String email, AsyncUserRequestListener listener) {
        if (PrefUtils.shouldSignIn(mContext)) {
            Request request = ApiUtils.buildPost(String.format(Locale.US, "%s/public/users/password/recover/%s", ApiUtils.BASE_URL, email));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while initiating user password recovery", response.code()));
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
            String passwordUpdateStr = JsonIOUtils.GSON.toJson(passwordUpdate);
            Request request = ApiUtils.buildPatch(String.format(Locale.US, "%s/users/password", ApiUtils.BASE_URL), passwordUpdateStr, PrefUtils.getAuhentication(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        ApiUserToken userToken = JsonIOUtils.GSON.fromJson(response.body().string(), ApiUserToken.class);
                        PrefUtils.signIn(mContext, userToken);
                        listener.onUserTokenReceived(userToken);
                    } else {
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while updating user password", response.code()));
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
            Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/users/friends", ApiUtils.BASE_URL), PrefUtils.getAuhentication(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
                        ApiFriendsAndRequests friendsAndRequests = JsonIOUtils.GSON.fromJson(response.body().string(), new TypeToken<ApiFriendsAndRequests>(){}.getType());

                        insertFriendsIntoDb(friendsAndRequests.getFriends(), false);
                        if (listener != null) {
                            listener.onFriendsAndRequestsReceived(friendsAndRequests);
                        }
                    } else {
                        Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d getting friends and requests", response.code()));
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

            for (FriendEntity friendEntity : AppDatabase.getInstance(mContext).friendDao().listFriends()) {
                referees.add(new ApiFriend(friendEntity.getId(), friendEntity.getPseudo()));
            }
        }

        return referees;
    }

    @Override
    public void sendFriendRequest(String friendPseudo, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            Request request = ApiUtils.buildPost(String.format(Locale.US, "%s/users/friends/request/%s", ApiUtils.BASE_URL, friendPseudo), PrefUtils.getAuhentication(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
            Request request = ApiUtils.buildPost(String.format(Locale.US, "%s/users/friends/accept/%s", ApiUtils.BASE_URL, friendRequest.getId()), PrefUtils.getAuhentication(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
            Request request = ApiUtils.buildPost(String.format(Locale.US, "%s/users/friends/reject/%s", ApiUtils.BASE_URL, friendRequest.getId()), PrefUtils.getAuhentication(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
            Request request = ApiUtils.buildDelete(String.format(Locale.US, "%s/users/friends/remove/%s", ApiUtils.BASE_URL, friend.getId()), PrefUtils.getAuhentication(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
