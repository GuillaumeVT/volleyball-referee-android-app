package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
import com.tonkar.volleyballreferee.business.data.db.FriendEntity;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.AsyncUserRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredUserService;

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
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, String.format(ApiUtils.USER_API_URL, pseudo), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        PrefUtils.createUser(mContext, pseudo);
                        if (listener != null) {
                            listener.onUserCreated();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d creating user", error.networkResponse.statusCode));
                        }
                        listener.onError();
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            if (listener != null) {
                listener.onError();
            }
        }
    }

    @Override
    public void downloadUser(AsyncUserRequestListener listener) {
        if (PrefUtils.isSyncOn(mContext) || PrefUtils.shouldCreateUser(mContext)) {
            Authentication authentication = PrefUtils.getAuthentication(mContext);
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.USERS_API_URL, new byte[0], authentication,
                    response -> {
                        ApiUser user = readUser(response);

                        if (user == null) {
                            if (listener != null) {
                                listener.onInternalError();
                            }
                        } else {
                            PrefUtils.signIn(mContext, Authentication.of(user.getId(), user.getPseudo(), authentication.getToken()));
                            insertFriendsIntoDb(user.getFriends(), false);

                            if (listener != null) {
                                listener.onUserReceived(user);
                            }
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d getting user", error.networkResponse.statusCode));
                            if (HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                if (listener != null) {
                                    listener.onNotFound();
                                }
                            } else {
                                if (listener != null) {
                                    listener.onError();
                                }
                            }
                        } else {
                            if (listener != null) {
                                listener.onError();
                            }
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            if (listener != null) {
                listener.onError();
            }
        }
    }

    @Override
    public void syncUser() {
        downloadUser(null);
    }

    @Override
    public void downloadFriendRequests(AsyncUserRequestListener listener) {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.FRIENDS_RECEIVED_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        List<ApiFriendRequest> friendRequests = readFriendRequests(response);

                        if (friendRequests == null) {
                            if (listener != null) {
                                listener.onInternalError();
                            }
                        } else {
                            if (listener != null) {
                                listener.onFriendRequestsReceived(friendRequests);
                            }
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d getting friend requests", error.networkResponse.statusCode));
                        }
                        listener.onError();
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            if (listener != null) {
                listener.onError();
            }
        }
    }

    @Override
    public boolean hasFriends() {
        return AppDatabase.getInstance(mContext).friendDao().count() > 0;
    }

    @Override
    public List<ApiFriend> listFriends() {
        List<ApiFriend> friends = new ArrayList<>();

        for (FriendEntity friendEntity : AppDatabase.getInstance(mContext).friendDao().listFriends()) {
            friends.add(new ApiFriend(friendEntity.getId(), friendEntity.getPseudo()));
        }

        return friends;
    }

    @Override
    public void sendFriendRequest(String friendPseudo) {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, String.format(ApiUtils.FRIENDS_REQUEST_API_URL, friendPseudo), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {},
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while sending friend request", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    @Override
    public void acceptFriendRequest(ApiFriendRequest friendRequest) {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, String.format(ApiUtils.FRIENDS_ACCEPT_API_URL, friendRequest.getId()), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> insertFriendIntoDb(friendRequest.getSenderId(), friendRequest.getSenderPseudo(), false),
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while accepting friend request", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    @Override
    public void rejectFriendRequest(ApiFriendRequest friendRequest) {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, String.format(ApiUtils.FRIENDS_REJECT_API_URL, friendRequest.getId()), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {},
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while accepting friend request", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    @Override
    public void removeFriend(String friendId) {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, String.format(ApiUtils.FRIENDS_REMOVE_API_URL, friendId), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> removeFriendFromDb(friendId),
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while accepting friend request", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private ApiUser readUser(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.USER_TYPE);
    }

    private List<ApiFriendRequest> readFriendRequests(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.FRIEND_REQUEST_LIST_TYPE);
    }

    private void insertFriendIntoDb(final String friendId, final String friendPseudo, boolean syncInsertion) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                FriendEntity friendEntity = new FriendEntity();
                friendEntity.setId(friendId);
                friendEntity.setPseudo(friendPseudo);
                AppDatabase.getInstance(mContext).friendDao().insert(friendEntity);
            }
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
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                AppDatabase.getInstance(mContext).friendDao().deleteAll();
                for (ApiFriend friend : friends) {
                    FriendEntity friendEntity = new FriendEntity();
                    friendEntity.setId(friend.getId());
                    friendEntity.setPseudo(friend.getPseudo());
                    AppDatabase.getInstance(mContext).friendDao().insert(friendEntity);
                }
            }
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            new Thread(runnable).start();
        }
    }
}
