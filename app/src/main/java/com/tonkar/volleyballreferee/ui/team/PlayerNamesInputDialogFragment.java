package com.tonkar.volleyballreferee.ui.team;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.ApiPlayer;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerNamesInputDialogFragment extends DialogFragment {

    private View      mView;
    private IBaseTeam mTeam;

    public static PlayerNamesInputDialogFragment newInstance(TeamType teamType) {
        PlayerNamesInputDialogFragment fragment = new PlayerNamesInputDialogFragment();
        Bundle args = new Bundle();
        args.putString("teamType", teamType.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public PlayerNamesInputDialogFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mView;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        TeamType teamType = TeamType.valueOf(getArguments().getString("teamType"));

        mView = getActivity().getLayoutInflater().inflate(R.layout.player_names_input_dialog, null);

        ListView playerNameList = mView.findViewById(R.id.player_name_list);
        playerNameList.setItemsCanFocus(true);
        PlayerNameListAdapter playerNameListAdapter = new PlayerNameListAdapter(getActivity().getLayoutInflater(), teamType, mTeam);
        playerNameList.setAdapter(playerNameListAdapter);

        AlertDialog alertDialog = new AlertDialog
                .Builder(getContext(), R.style.AppTheme_Dialog)
                .setTitle(R.string.players).setView(mView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dismiss())
                .create();

        playerNameList.post(() -> {
            alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            mView.requestLayout();
        });

        return alertDialog;
    }

    public void setTeam(IBaseTeam team) {
        mTeam = team;
    }

    private static class PlayerNameListAdapter extends BaseAdapter {

        private final LayoutInflater  mLayoutInflater;
        private final TeamType        mTeamType;
        private final IBaseTeam       mTeam;
        private final List<ApiPlayer> mPlayers;

        private PlayerNameListAdapter(LayoutInflater layoutInflater, TeamType teamType, IBaseTeam team) {
            mLayoutInflater = layoutInflater;
            mTeamType = teamType;
            mTeam = team;
            mPlayers = new ArrayList<>(mTeam.getPlayers(mTeamType));
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
        public View getView(int position, View view, ViewGroup parent) {
            View playerNameView = mLayoutInflater.inflate(R.layout.player_name_item, parent, false);

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
                    ApiPlayer player = mPlayers.get(position);
                    mTeam.setPlayerName(mTeamType, player.getNum(), name);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            return playerNameView;
        }
    }
}
