package com.tonkar.volleyballreferee.ui.team;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.stored.api.ApiPlayer;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerNamesInputDialogFragment extends DialogFragment {

    private       View                  mView;
    private       PlayerNameListAdapter mPlayerNameListAdapter;
    private       TeamType              mTeamType;
    private final List<ApiPlayer>       mPlayers;

    public static PlayerNamesInputDialogFragment newInstance(TeamType teamType) {
        PlayerNamesInputDialogFragment fragment = new PlayerNamesInputDialogFragment();
        Bundle args = new Bundle();
        args.putString("teamType", teamType.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public PlayerNamesInputDialogFragment() {
        mPlayers = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mView;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        mTeamType = TeamType.valueOf(getArguments().getString("teamType"));

        mView = getActivity().getLayoutInflater().inflate(R.layout.player_names_input_dialog, null);

        ListView playerNameList = mView.findViewById(R.id.player_name_list);
        mPlayerNameListAdapter = new PlayerNameListAdapter(getActivity().getLayoutInflater());
        playerNameList.setAdapter(mPlayerNameListAdapter);

        return new AlertDialog
                .Builder(getContext(), R.style.AppTheme_Dialog)
                .setTitle(R.string.players).setView(mView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dismiss())
                .create();
    }

    public void setTeam(IBaseTeam team) {
        mTeamType = TeamType.valueOf(getArguments().getString("teamType"));
        mPlayers.clear();
        mPlayers.addAll(team.getPlayers(mTeamType));
        if (mPlayerNameListAdapter != null) {
            mPlayerNameListAdapter.notifyDataSetChanged();
        }
    }

    private class PlayerNameListAdapter extends BaseAdapter {

        private final LayoutInflater  mLayoutInflater;

        private PlayerNameListAdapter(LayoutInflater layoutInflater) {
            mLayoutInflater = layoutInflater;
        }

        @Override
        public int getCount() {
            return mPlayers.size();
        }

        @Override
        public Object getItem(int position) {
            return mPlayers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View playerNameView = mLayoutInflater.inflate(R.layout.player_name_item, null);

            TextInputLayout playerNameInputTextLayout = playerNameView.findViewById(R.id.player_name_input_layout);
            EditText playerNameInputText = playerNameView.findViewById(R.id.player_name_input_text);

            ApiPlayer player = mPlayers.get(position);
            playerNameInputText.setText(player.getName());
            playerNameInputTextLayout.setHint(String.format(Locale.getDefault(), "#%d", player.getNum()));

            playerNameInputText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String name = s.toString().trim();
                    if (isAdded() && !name.isEmpty()) {
                        ApiPlayer player = mPlayers.get(position);
                        player.setName(name);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            return playerNameView;
        }
    }
}