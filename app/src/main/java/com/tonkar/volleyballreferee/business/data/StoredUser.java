package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
import com.tonkar.volleyballreferee.business.data.db.FriendEntity;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.AsyncFriendRequestListener;
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
            ApiUser user = new ApiUser();
            user.setId(userId);
            user.setPseudo(pseudo);
            user.setFriends(new ArrayList<>());
            final byte[] bytes = writeUser(user).getBytes();

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, ApiUtils.USER_API_URL, bytes,
                    response -> {
                        PrefUtils.storeUserPseudo(mContext, pseudo);
                        listener.onUserCreated(user);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d creating user", error.networkResponse.statusCode));
                            listener.onError(error.networkResponse.statusCode);
                        } else {
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void downloadUser(AsyncUserRequestListener listener) {
        if (PrefUtils.canSync(mContext) || PrefUtils.shouldCreateUser(mContext)) {
            Authentication authentication = PrefUtils.getAuthentication(mContext);
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.USERS_API_URL, new byte[0], authentication,
                    response -> {
                        ApiUser user = readUser(response);

                        if (user == null) {
                            if (listener != null) {
                                listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
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
                            if (listener != null) {
                                listener.onError(error.networkResponse.statusCode);
                            }
                        } else {
                            if (listener != null) {
                                listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                            }
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
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
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.FRIENDS_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        ApiFriendsAndRequests friendsAndRequests = readFriendsAndRequests(response);

                        if (friendsAndRequests == null) {
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        } else {
                            listener.onFriendsAndRequestsReceived(friendsAndRequests);
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d getting friends and requests", error.networkResponse.statusCode));
                            listener.onError(error.networkResponse.statusCode);
                        } else {
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
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
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, String.format(ApiUtils.FRIENDS_REQUEST_API_URL, friendPseudo), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> listener.onFriendRequestSent(friendPseudo),
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while sending friend request", error.networkResponse.statusCode));
                            listener.onError(error.networkResponse.statusCode);
                        } else {
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void acceptFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, String.format(ApiUtils.FRIENDS_ACCEPT_API_URL, friendRequest.getId()), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        insertFriendIntoDb(friendRequest.getSenderId(), friendRequest.getSenderPseudo(), false);
                        listener.onFriendRequestAccepted(friendRequest);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while accepting friend request", error.networkResponse.statusCode));
                            listener.onError(error.networkResponse.statusCode);
                        } else {
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void rejectFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, String.format(ApiUtils.FRIENDS_REJECT_API_URL, friendRequest.getId()), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> listener.onFriendRequestRejected(friendRequest),
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while rejecting friend request", error.networkResponse.statusCode));
                            listener.onError(error.networkResponse.statusCode);
                        } else {
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void removeFriend(ApiFriend friend, AsyncFriendRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, String.format(ApiUtils.FRIENDS_REMOVE_API_URL, friend.getId()), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        removeFriendFromDb(friend.getId());
                        listener.onFriendRemoved(friend);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_USER, String.format(Locale.getDefault(), "Error %d while removing friend", error.networkResponse.statusCode));
                            listener.onError(error.networkResponse.statusCode);
                        } else {
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
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
