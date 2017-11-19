package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ToggleButton;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

public class LiberoSetupFragment extends Fragment implements TeamClient, TeamColorDialogFragment.TeamColorSelectionListener {

    private TeamType          mTeamType;
    private IndoorTeamService mTeamService;
    private Button            mLiberoColorButton;

    public LiberoSetupFragment() {
    }

    public static LiberoSetupFragment newInstance(TeamType teamType) {
        LiberoSetupFragment fragment = new LiberoSetupFragment();
        Bundle args = new Bundle();
        args.putString(TeamType.class.getName(), teamType.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-LSActivity", "Create libero setup fragment");

        final String teamTypeStr = getArguments().getString(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        setTeamService(ServicesProvider.getInstance().getTeamService());
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = (IndoorTeamService) teamService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_libero_setup, container, false);

        mLiberoColorButton = view.findViewById(R.id.libero_color_button);
        if (mTeamService.getLiberoColor(mTeamType) == Integer.MIN_VALUE) {
            onTeamColorSelected(ShirtColors.getRandomShirtColor(getActivity()));
        } else {
            onTeamColorSelected(mTeamService.getLiberoColor(mTeamType));
        }
        mLiberoColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectLiberoColor();
            }
        });

        final GridView liberoNumbersGrid = view.findViewById(R.id.team_libero_numbers_grid);
        final LiberoAdapter liberoAdapter = new LiberoAdapter(getActivity());
        liberoNumbersGrid.setAdapter(liberoAdapter);

        if (savedInstanceState != null) {
            TeamColorDialogFragment teamColorDialogFragment = (TeamColorDialogFragment) getActivity().getFragmentManager().findFragmentByTag(mTeamType.toString() + "select_libero_color");
            if (teamColorDialogFragment != null) {
                teamColorDialogFragment.setTeamColorSelectionListener(this);
            }
        }

        return view;
    }

    private void selectLiberoColor() {
        Log.i("VBR-LSActivity", String.format("Select %s team libero color", mTeamType.toString()));
        TeamColorDialogFragment teamColorDialogFragment = TeamColorDialogFragment.newInstance();
        teamColorDialogFragment.setTeamColorSelectionListener(this);
        teamColorDialogFragment.show(getActivity().getFragmentManager(), mTeamType.toString() + "select_libero_color");
    }

    @Override
    public void onTeamColorSelected(int color) {
        Log.i("VBR-LSActivity", String.format("Update %s team libero color", mTeamType.toString()));
        UiUtils.colorTeamButton(getActivity(), color, mLiberoColorButton);
        mTeamService.setLiberoColor(mTeamType, color);
    }

    private class LiberoAdapter extends BaseAdapter {

        private final Context mContext;

        private LiberoAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mTeamService.getNumberOfPlayers(mTeamType);
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
            final int playerShirtNumber = mTeamService.getPlayers(mTeamType).get(position);
            final ToggleButton button;

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
            button.setChecked(mTeamService.isLibero(mTeamType, playerShirtNumber));
            button.setTextSize(16);

            button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final int number = Integer.parseInt(buttonView.getText().toString());
                    if (isChecked) {
                        if (mTeamService.canAddLibero(mTeamType)) {
                            Log.i("VBR-LSActivity", String.format("Checked #%d player of %s team as libero", number, mTeamType.toString()));
                            mTeamService.addLibero(mTeamType, number);
                        } else {
                            button.setChecked(false);
                        }

                    } else {
                        Log.i("VBR-LSActivity", String.format("Unchecked #%d player of %s team as libero", number, mTeamType.toString()));
                        mTeamService.removeLibero(mTeamType, number);
                    }
                }
            });

            return button;
        }
    }
}
