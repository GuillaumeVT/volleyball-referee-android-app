package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class AdditionalSetupFragment extends Fragment {

    private LayoutInflater        mLayoutInflater;
    private TeamType              mTeamType;
    private IndoorTeamService     mIndoorTeamService;
    private Button                mLiberoColorButton;
    private Button                mCaptainButton;
    private LiberoAdapter         mLiberoAdapter;

    public AdditionalSetupFragment() {
    }

    public static AdditionalSetupFragment newInstance(TeamType teamType) {
        AdditionalSetupFragment fragment = new AdditionalSetupFragment();
        Bundle args = new Bundle();
        args.putString(TeamType.class.getName(), teamType.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-ASActivity", "Create additional setup fragment");
        mLayoutInflater = inflater;
        View view = mLayoutInflater.inflate(R.layout.fragment_additional_setup, container, false);

        final String teamTypeStr = getArguments().getString(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        mIndoorTeamService = (IndoorTeamService) ServicesProvider.getInstance().getTeamService();

        mCaptainButton = view.findViewById(R.id.team_captain_number_button);
        updateCaptain();
        mCaptainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCaptain();
            }
        });

        final GridView liberoNumbersGrid = view.findViewById(R.id.team_libero_numbers_grid);
        mLiberoAdapter = new LiberoAdapter(getActivity(), mIndoorTeamService.getLiberoColor(mTeamType));
        liberoNumbersGrid.setAdapter(mLiberoAdapter);

        mLiberoColorButton = view.findViewById(R.id.libero_color_button);
        if (mIndoorTeamService.getLiberoColor(mTeamType) == Integer.MIN_VALUE) {
            teamColorSelected(ShirtColors.getRandomShirtColor(getActivity()));
        } else {
            teamColorSelected(mIndoorTeamService.getLiberoColor(mTeamType));
        }
        mLiberoColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectLiberoColor();
            }
        });

        return view;
    }

    private void selectLiberoColor() {
        Log.i("VBR-ASActivity", String.format("Select %s team libero color", mTeamType.toString()));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(mLayoutInflater, getContext(), getResources().getString(R.string.select_shirts_color)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    public void teamColorSelected(int color) {
        Log.i("VBR-ASActivity", String.format("Update %s team libero color", mTeamType.toString()));
        UiUtils.colorTeamButton(getActivity(), color, mLiberoColorButton);
        mIndoorTeamService.setLiberoColor(mTeamType, color);
        mLiberoAdapter.setColor(color);
    }

    private void updateCaptain() {
        int captain = mIndoorTeamService.getCaptain(mTeamType);
        if (captain < 1 || mIndoorTeamService.isLibero(mTeamType, captain)) {
            captain = mIndoorTeamService.getPossibleCaptains(mTeamType).iterator().next();
        }

        captainUpdated(mTeamType, captain);
    }

    private void captainUpdated(TeamType teamType, int number) {
        Log.i("VBR-ASActivity", String.format("Update %s team captain", mTeamType.toString()));
        mIndoorTeamService.setCaptain(teamType, number);
        mCaptainButton.setText(String.valueOf(number));
        UiUtils.styleBaseIndoorTeamButton(getContext(), mIndoorTeamService, mTeamType, number, mCaptainButton);
    }

    private void selectCaptain() {
        Log.i("VBR-ASActivity", String.format("Select %s team captain", mTeamType.toString()));
        IndoorPlayerSelectionDialog playerSelectionDialog = new IndoorPlayerSelectionDialog(mLayoutInflater, getContext(), getResources().getString(R.string.select_captain), mIndoorTeamService,
                mTeamType, mIndoorTeamService.getPossibleCaptains(mTeamType)) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                captainUpdated(mTeamType, selectedNumber);
            }
        };
        playerSelectionDialog.show();
    }

    private class LiberoAdapter extends BaseAdapter {

        private final Context       mContext;
        private final List<Integer> mPlayers;
        private       int           mColor;

        private LiberoAdapter(Context context, int color) {
            mContext = context;
            mPlayers = new ArrayList<>(mIndoorTeamService.getPlayers(mTeamType));
            mColor = color;
        }

        @Override
        public int getCount() {
            return mPlayers.size();
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
            final int playerShirtNumber = mPlayers.get(position);
            final PlayerToggleButton button;

            if (view == null) {
                button = new PlayerToggleButton(mContext);
            } else {
                button = (PlayerToggleButton) view;
            }

            button.setText(String.valueOf(playerShirtNumber));
            button.setChecked(mIndoorTeamService.isLibero(mTeamType, playerShirtNumber));
            button.setColor(mContext, mColor);

            button.setOnCheckedChangeListener(new PlayerToggleButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(PlayerToggleButton button, boolean isChecked) {
                    final int number = Integer.parseInt(button.getText().toString());
                    if (isChecked) {
                        if (mIndoorTeamService.canAddLibero(mTeamType)) {
                            Log.i("VBR-ASActivity", String.format("Checked #%d player of %s team as libero", number, mTeamType.toString()));
                            mIndoorTeamService.addLibero(mTeamType, number);
                            updateCaptain();
                        } else {
                            button.setChecked(false);
                        }
                    } else {
                        Log.i("VBR-ASActivity", String.format("Unchecked #%d player of %s team as libero", number, mTeamType.toString()));
                        mIndoorTeamService.removeLibero(mTeamType, number);
                        updateCaptain();
                    }
                }
            });

            return button;
        }

        public void setColor(int color) {
            mColor = color;
            notifyDataSetChanged();
        }
    }
}
