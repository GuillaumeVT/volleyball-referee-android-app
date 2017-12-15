package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

public class TeamSetupFragment extends Fragment {

    private TeamType      mTeamType;
    private TeamService   mTeamService;
    private Button        mTeamColorButton;
    private PlayerAdapter mPlayerAdapter;

    public TeamSetupFragment() {
    }

    public static TeamSetupFragment newInstance(TeamType teamType) {
        TeamSetupFragment fragment = new TeamSetupFragment();
        Bundle args = new Bundle();
        args.putString(TeamType.class.getName(), teamType.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-TSActivity", "Create team setup fragment");
        View view = inflater.inflate(R.layout.fragment_team_setup, container, false);

        final String teamTypeStr = getArguments().getString(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        mTeamService = ServicesProvider.getInstance().getTeamService();

        final EditText teamNameInput = view.findViewById(R.id.team_name_input_text);
        teamNameInput.setText(mTeamService.getTeamName(mTeamType));

        switch (mTeamType) {
            case HOME:
                teamNameInput.setHint(R.string.home_team_hint);
                break;
            case GUEST:
                teamNameInput.setHint(R.string.guest_team_hint);
                break;
        }

        teamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-TSActivity", String.format("Update %s team name", mTeamType.toString()));
                mTeamService.setTeamName(mTeamType, s.toString());
                computeNextButtonActivation();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final GridView teamNumbersGrid = view.findViewById(R.id.team_member_numbers_grid);
        mPlayerAdapter = new PlayerAdapter(getActivity(), mTeamService.getTeamColor(mTeamType));
        teamNumbersGrid.setAdapter(mPlayerAdapter);

        mTeamColorButton = view.findViewById(R.id.team_color_button);
        if (mTeamService.getTeamColor(mTeamType) == Integer.MIN_VALUE) {
            teamColorSelected(ShirtColors.getRandomShirtColor(getActivity()));
        } else {
            teamColorSelected(mTeamService.getTeamColor(mTeamType));
        }
        mTeamColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTeamColor();
            }
        });

        computeNextButtonActivation();

        return view;
    }

    private void selectTeamColor() {
        Log.i("VBR-TSActivity", String.format("Select %s team color", mTeamType.toString()));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), getContext(), getResources().getString(R.string.select_shirts_color)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    private void teamColorSelected(int color) {
        Log.i("VBR-TSActivity", String.format("Update %s team color", mTeamType.toString()));
        UiUtils.colorTeamButton(getActivity(), color, mTeamColorButton);
        mTeamService.setTeamColor(mTeamType, color);
        mPlayerAdapter.setColor(color);
    }

    private class PlayerAdapter extends BaseAdapter {

        private final Context mContext;
        private       int     mColor;

        private PlayerAdapter(Context context, int color) {
            mContext = context;
            mColor = color;
        }

        @Override
        public int getCount() {
            return 25;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            final int playerShirtNumber = position + 1;
            PlayerToggleButton button;

            if (view == null) {
                button = new PlayerToggleButton(mContext);
            } else {
                button = (PlayerToggleButton) view;
            }

            button.setText(String.valueOf(playerShirtNumber));
            button.setChecked(mTeamService.hasPlayer(mTeamType, playerShirtNumber));
            button.setColor(mContext, mColor);

            button.setOnCheckedChangeListener(new PlayerToggleButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(PlayerToggleButton button, boolean isChecked) {
                    final int number = Integer.parseInt(button.getText().toString());
                    if (isChecked) {
                        Log.i("VBR-TSActivity", String.format("Checked #%d player of %s team", number, mTeamType.toString()));
                        mTeamService.addPlayer(mTeamType, number);
                    } else {
                        Log.i("VBR-TSActivity", String.format("Unchecked #%d player of %s team", number, mTeamType.toString()));
                        mTeamService.removePlayer(mTeamType, number);
                    }
                    computeNextButtonActivation();
                }
            });

            return button;
        }

        public void setColor(int color) {
            mColor = color;
            notifyDataSetChanged();
        }
    }

    void computeNextButtonActivation() {
        TeamsSetupActivity activity = (TeamsSetupActivity) getActivity();
        if (activity != null) {
            activity.computeNextButtonActivation();
        }
    }

}
