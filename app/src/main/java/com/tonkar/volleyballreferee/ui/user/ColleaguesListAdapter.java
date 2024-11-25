package com.tonkar.volleyballreferee.ui.user;

import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ColleaguesListAdapter extends ArrayAdapter<ColleagueItem> {

    private final LayoutInflater             mLayoutInflater;
    private final StoredUserService          mStoredUserService;
    private final AsyncFriendRequestListener mListener;
    private final List<ColleagueItem>        mColleagueItems;
    private final List<ColleagueItem>        mFilteredColleagueItems;
    private final NameFilter                 mNameFilter;

    ColleaguesListAdapter(Context context, LayoutInflater layoutInflater, AsyncFriendRequestListener listener) {
        super(context, android.R.layout.simple_list_item_1, new ArrayList<>());
        mLayoutInflater = layoutInflater;
        mStoredUserService = new StoredUserManager(context);
        mListener = listener;
        mColleagueItems = new ArrayList<>();
        mFilteredColleagueItems = new ArrayList<>();
        mNameFilter = new NameFilter();
    }

    @Override
    public int getCount() {
        return mFilteredColleagueItems.size();
    }

    @Override
    public ColleagueItem getItem(int index) {
        return mFilteredColleagueItems.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
        ColleagueItem colleagueItem = mFilteredColleagueItems.get(index);

        switch (colleagueItem.getItemType()) {
            case FRIEND -> view = createFriendItem(colleagueItem);
            case RECEIVED -> view = createReceivedFriendRequestItem(colleagueItem);
            case SENT -> view = createSentFriendRequestItem(colleagueItem);
            default -> {
            }
        }

        return view;
    }

    private View createFriendItem(ColleagueItem colleagueItem) {
        View view = mLayoutInflater.inflate(R.layout.friend_item, null);

        TextView text = view.findViewById(R.id.friend_text);
        text.setText(colleagueItem.getFriend().getPseudo());

        Button removeFriendButton = view.findViewById(R.id.remove_friend_button);
        removeFriendButton.setOnClickListener(button -> {
            Context context = getContext();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
                    .setTitle(context.getString(R.string.remove))
                    .setMessage(String.format(Locale.getDefault(), context.getString(R.string.remove_colleague_question),
                                              colleagueItem.getFriend().getPseudo()))
                    .setPositiveButton(android.R.string.yes,
                                       (dialog, which) -> mStoredUserService.removeFriend(colleagueItem.getFriend(), mListener))
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {});
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, context.getResources());
        });

        return view;
    }

    private View createReceivedFriendRequestItem(ColleagueItem colleagueItem) {
        View view = mLayoutInflater.inflate(R.layout.received_friend_request_item, null);

        TextView text = view.findViewById(R.id.friend_text);
        text.setText(String.format(Locale.getDefault(), getContext().getString(R.string.received_colleague_request),
                                   colleagueItem.getFriendRequest().getSenderPseudo()));

        Button acceptFriendButton = view.findViewById(R.id.accept_friend_button);
        acceptFriendButton.setOnClickListener(button -> {
            Context context = getContext();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
                    .setTitle(context.getString(R.string.accept))
                    .setMessage(String.format(Locale.getDefault(), context.getString(R.string.accept_colleague_question),
                                              colleagueItem.getFriendRequest().getSenderPseudo()))
                    .setPositiveButton(android.R.string.yes,
                                       (dialog, which) -> mStoredUserService.acceptFriendRequest(colleagueItem.getFriendRequest(),
                                                                                                 mListener))
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {});
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, context.getResources());
        });

        Button rejectFriendButton = view.findViewById(R.id.reject_friend_button);
        rejectFriendButton.setOnClickListener(button -> {
            Context context = getContext();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
                    .setTitle(context.getString(R.string.reject))
                    .setMessage(String.format(Locale.getDefault(), context.getString(R.string.reject_colleague_question),
                                              colleagueItem.getFriendRequest().getSenderPseudo()))
                    .setPositiveButton(android.R.string.yes,
                                       (dialog, which) -> mStoredUserService.rejectFriendRequest(colleagueItem.getFriendRequest(),
                                                                                                 mListener))
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {});
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, context.getResources());
        });

        return view;
    }

    private View createSentFriendRequestItem(ColleagueItem colleagueItem) {
        View view = mLayoutInflater.inflate(R.layout.sent_friend_request_item, null);

        TextView text = view.findViewById(R.id.friend_text);
        text.setText(String.format(Locale.getDefault(), getContext().getString(R.string.sent_colleague_request),
                                   colleagueItem.getFriendRequest().getReceiverPseudo()));

        return view;
    }

    @Override
    public @NonNull Filter getFilter() {
        return mNameFilter;
    }

    void updateFriendsAndRequests(FriendsAndRequestsDto friendsAndRequests) {
        mColleagueItems.clear();
        mFilteredColleagueItems.clear();

        for (FriendRequestDto friendRequest : friendsAndRequests.getReceivedFriendRequests()) {
            mColleagueItems.add(new ColleagueItem(ColleagueItem.ItemType.RECEIVED, friendRequest));
        }

        for (FriendRequestDto friendRequest : friendsAndRequests.getSentFriendRequests()) {
            mColleagueItems.add(new ColleagueItem(ColleagueItem.ItemType.SENT, friendRequest));
        }

        for (FriendDto friend : friendsAndRequests.getFriends()) {
            mColleagueItems.add(new ColleagueItem(ColleagueItem.ItemType.FRIEND, friend));
        }

        mFilteredColleagueItems.addAll(mColleagueItems);
        notifyDataSetChanged();
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (StringUtils.isBlank(prefix)) {
                results.values = mColleagueItems;
                results.count = mColleagueItems.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<ColleagueItem> matchValues = new ArrayList<>();

                for (ColleagueItem colleagueItem : mColleagueItems) {
                    if (lowerCaseText.isEmpty()) {
                        matchValues.add(colleagueItem);
                    } else {
                        switch (colleagueItem.getItemType()) {
                            case FRIEND:
                                if (colleagueItem.getFriend().getPseudo().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                                    matchValues.add(colleagueItem);
                                }
                                break;
                            case SENT:
                                if (colleagueItem
                                        .getFriendRequest()
                                        .getReceiverPseudo()
                                        .toLowerCase(Locale.getDefault())
                                        .contains(lowerCaseText)) {
                                    matchValues.add(colleagueItem);
                                }
                                break;
                            case RECEIVED:
                                if (colleagueItem
                                        .getFriendRequest()
                                        .getSenderPseudo()
                                        .toLowerCase(Locale.getDefault())
                                        .contains(lowerCaseText)) {
                                    matchValues.add(colleagueItem);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredColleagueItems.clear();

            if (results.values != null) {
                mFilteredColleagueItems.addAll((Collection<ColleagueItem>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
