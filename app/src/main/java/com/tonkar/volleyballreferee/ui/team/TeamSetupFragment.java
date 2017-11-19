package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ToggleButton;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

public class TeamSetupFragment extends Fragment implements TeamClient, TeamColorDialogFragment.TeamColorSelectionListener {

    private TeamType    mTeamType;
    private TeamService mTeamService;
    private Button      mTeamColorButton;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-TSActivity", "Create team setup fragment");

        final String teamTypeStr = getArguments().getString(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        setTeamService(ServicesProvider.getInstance().getTeamService());
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_setup, container, false);

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

        mTeamColorButton = view.findViewById(R.id.team_color_button);
        if (mTeamService.getTeamColor(mTeamType) == Integer.MIN_VALUE) {
            onTeamColorSelected(ShirtColors.getRandomShirtColor(getActivity()));
        } else {
            onTeamColorSelected(mTeamService.getTeamColor(mTeamType));
        }
        mTeamColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTeamColor();
            }
        });

        final GridView teamNumbersGrid = view.findViewById(R.id.team_member_numbers_grid);
        final PlayerAdapter playerAdapter = new PlayerAdapter(getActivity());
        teamNumbersGrid.setAdapter(playerAdapter);

        if (savedInstanceState != null) {
            TeamColorDialogFragment teamColorDialogFragment = (TeamColorDialogFragment) getActivity().getFragmentManager().findFragmentByTag(mTeamType.toString() + "select_team_color");
            if (teamColorDialogFragment != null) {
                teamColorDialogFragment.setTeamColorSelectionListener(this);
            }
        }

        computeNextButtonActivation();

        return view;
    }

    private void selectTeamColor() {
        Log.i("VBR-TSActivity", String.format("Select %s team color", mTeamType.toString()));
        TeamColorDialogFragment teamColorDialogFragment = TeamColorDialogFragment.newInstance();
        teamColorDialogFragment.setTeamColorSelectionListener(this);
        teamColorDialogFragment.show(getActivity().getFragmentManager(), mTeamType.toString() + "select_team_color");
    }

    @Override
    public void onTeamColorSelected(int colorId) {
        Log.i("VBR-TSActivity", String.format("Update %s team color", mTeamType.toString()));
        UiUtils.colorTeamButton(getActivity(), colorId, mTeamColorButton);
        mTeamService.setTeamColor(mTeamType, colorId);
    }

    private class PlayerAdapter extends BaseAdapter {

        private final Context mContext;

        private PlayerAdapter(Context context) {
            mContext = context;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            final int playerShirtNumber = position + 1;
            ToggleButton button;

            if (convertView == null) {
                button = new ToggleButton(mContext);
            } else {
                button = (ToggleButton) convertView;
            }

            button.setText(String.valueOf(playerShirtNumber));
            button.setTextOn(String.valueOf(playerShirtNumber));
            button.setTextOff(String.valueOf(playerShirtNumber));
            button.setTextColor(ContextCompat.getColorStateList(mContext, R.color.toggle_button_text_color));
            button.setBackground(ContextCompat.getDrawable(mContext, R.drawable.toggle_button_color));
            button.setChecked(mTeamService.hasPlayer(mTeamType, playerShirtNumber));
            button.setTextSize(16);

            button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final int number = Integer.parseInt(buttonView.getText().toString());
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
    }

    void computeNextButtonActivation() {
        ((TeamsSetupActivity) getActivity()).computeNextButtonActivation();
    }

}
