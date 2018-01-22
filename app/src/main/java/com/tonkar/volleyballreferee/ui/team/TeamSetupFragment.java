package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.data.SavedTeamActivity;
import com.tonkar.volleyballreferee.ui.data.SavedTeamsListAdapter;

import java.util.ArrayList;
import java.util.List;

public class TeamSetupFragment extends Fragment {

    private LayoutInflater        mLayoutInflater;
    private TeamType              mTeamType;
    private BaseIndoorTeamService mIndoorTeamService;
    private Button                mTeamColorButton;
    private PlayerAdapter         mPlayerAdapter;
    private Button                mLiberoColorButton;
    private Button                mCaptainButton;
    private LiberoAdapter         mLiberoAdapter;
    private ImageButton           mGenderButton;

    public TeamSetupFragment() {
    }

    public static TeamSetupFragment newInstance(TeamType teamType) {
        return newInstance(teamType, true);
    }

    public static TeamSetupFragment newInstance(TeamType teamType, boolean isGameContext) {
        TeamSetupFragment fragment = new TeamSetupFragment();
        Bundle args = new Bundle();
        args.putString(TeamType.class.getName(), teamType.toString());
        args.putBoolean("is_game", isGameContext);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-TSActivity", "Create team setup fragment");
        mLayoutInflater = inflater;
        View view = mLayoutInflater.inflate(R.layout.fragment_team_setup, container, false);

        if (!ServicesProvider.getInstance().areServicesAvailable()) {
            ServicesProvider.getInstance().restoreGameServiceForSetup(getActivity().getApplicationContext());
        }

        final String teamTypeStr = getArguments().getString(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        final boolean isGameContext = getArguments().getBoolean("is_game");

        if (isGameContext) {
            mIndoorTeamService = (BaseIndoorTeamService) ServicesProvider.getInstance().getTeamService();
        } else {
            mIndoorTeamService = ServicesProvider.getInstance().getSavedTeamsService().getCurrentTeam();
        }

        final AutoCompleteTextView teamNameInput = view.findViewById(R.id.team_name_input_text);
        mTeamColorButton = view.findViewById(R.id.team_color_button);
        final GridView teamNumbersGrid = view.findViewById(R.id.team_member_numbers_grid);
        mCaptainButton = view.findViewById(R.id.team_captain_number_button);
        final GridView liberoNumbersGrid = view.findViewById(R.id.team_libero_numbers_grid);
        mLiberoColorButton = view.findViewById(R.id.libero_color_button);

        switch (mTeamType) {
            case HOME:
                teamNameInput.setHint(R.string.home_team_hint);
                break;
            case GUEST:
                teamNameInput.setHint(R.string.guest_team_hint);
                break;
        }

        final String teamName = mIndoorTeamService.getTeamName(mTeamType);

        teamNameInput.setText(teamName);

        if (isGameContext) {
            teamNameInput.setThreshold(2);
            teamNameInput.setAdapter(new SavedTeamsListAdapter(getContext(), getLayoutInflater(), ServicesProvider.getInstance().getSavedTeamsService().getSavedTeamServiceList()));
            teamNameInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                    BaseIndoorTeamService indoorTeamService = (BaseIndoorTeamService) teamNameInput.getAdapter().getItem(index);
                    teamNameInput.setText(indoorTeamService.getTeamName(mTeamType));
                    ServicesProvider.getInstance().getSavedTeamsService().copyTeam(indoorTeamService, mIndoorTeamService, mTeamType);
                    if (getActivity() instanceof TeamsSetupActivity) {
                        getActivity().recreate();
                    }
                }
            });
        }

        if (!teamName.isEmpty()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        teamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-TSActivity", String.format("Update %s team name", mTeamType.toString()));
                mIndoorTeamService.setTeamName(mTeamType, s.toString());
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mPlayerAdapter = new PlayerAdapter(getActivity(), mIndoorTeamService.getTeamColor(mTeamType));
        teamNumbersGrid.setAdapter(mPlayerAdapter);

        if (mIndoorTeamService.getTeamColor(mTeamType) == Integer.MIN_VALUE) {
            teamColorSelected(ShirtColors.getRandomShirtColor(getActivity()));
        } else {
            teamColorSelected(mIndoorTeamService.getTeamColor(mTeamType));
        }
        mTeamColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.animate(getContext(), mTeamColorButton);
                selectTeamColor();
            }
        });

        updateCaptain();
        mCaptainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.animate(getContext(), mCaptainButton);
                selectCaptain();
            }
        });

        mLiberoAdapter = new LiberoAdapter(getActivity(), mIndoorTeamService.getLiberoColor(mTeamType));
        liberoNumbersGrid.setAdapter(mLiberoAdapter);

        if (mIndoorTeamService.getLiberoColor(mTeamType) == Integer.MIN_VALUE) {
            liberoColorSelected(ShirtColors.getRandomShirtColor(getActivity()));
        } else {
            liberoColorSelected(mIndoorTeamService.getLiberoColor(mTeamType));
        }
        mLiberoColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.animate(getContext(), mLiberoColorButton);
                selectLiberoColor();
            }
        });

        mGenderButton = view.findViewById(R.id.select_gender_button);
        updateGender(mIndoorTeamService.getGenderType(mTeamType));
        mGenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.animate(getContext(), mGenderButton);
                GenderType genderType = mIndoorTeamService.getGenderType(mTeamType).next();
                updateGender(genderType);
            }
        });

        computeConfirmItemVisibility();

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
        mIndoorTeamService.setTeamColor(mTeamType, color);
        mPlayerAdapter.setColor(color);
        updateCaptain();
    }

    private class PlayerAdapter extends BaseAdapter {

        private final Context mContext;
        private       int     mColor;
        private final int     mCount;

        private PlayerAdapter(Context context, int color) {
            mContext = context;
            mColor = color;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            mCount = Integer.valueOf(sharedPreferences.getString("pref_number_of_shirts", "25"));
        }

        @Override
        public int getCount() {
            return mCount;
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
            button.setChecked(mIndoorTeamService.hasPlayer(mTeamType, playerShirtNumber));
            button.setColor(mContext, mColor);

            button.setOnCheckedChangeListener(new PlayerToggleButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(PlayerToggleButton button, boolean isChecked) {
                    UiUtils.animate(mContext, button);
                    final int number = Integer.parseInt(button.getText().toString());
                    if (isChecked) {
                        Log.i("VBR-TSActivity", String.format("Checked #%d player of %s team", number, mTeamType.toString()));
                        mIndoorTeamService.addPlayer(mTeamType, number);
                        updateCaptain();
                    } else {
                        Log.i("VBR-TSActivity", String.format("Unchecked #%d player of %s team", number, mTeamType.toString()));
                        mIndoorTeamService.removePlayer(mTeamType, number);
                    }
                    mLiberoAdapter.notifyDataSetChanged();
                    computeConfirmItemVisibility();
                }
            });

            return button;
        }

        public void setColor(int color) {
            mColor = color;
            notifyDataSetChanged();
        }
    }

    private void selectLiberoColor() {
        Log.i("VBR-TSActivity", String.format("Select %s team libero color", mTeamType.toString()));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(mLayoutInflater, getContext(), getResources().getString(R.string.select_shirts_color)) {
            @Override
            public void onColorSelected(int selectedColor) {
                liberoColorSelected(selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    private void liberoColorSelected(int color) {
        Log.i("VBR-TSActivity", String.format("Update %s team libero color", mTeamType.toString()));
        UiUtils.colorTeamButton(getActivity(), color, mLiberoColorButton);
        mIndoorTeamService.setLiberoColor(mTeamType, color);
        mLiberoAdapter.setColor(color);
    }

    private void updateCaptain() {
        int captain = mIndoorTeamService.getCaptain(mTeamType);
        if ((captain < 1 || mIndoorTeamService.isLibero(mTeamType, captain)) && !mIndoorTeamService.getPossibleCaptains(mTeamType).isEmpty()) {
            captain = mIndoorTeamService.getPossibleCaptains(mTeamType).iterator().next();
        }

        captainUpdated(mTeamType, captain);
    }

    private void captainUpdated(TeamType teamType, int number) {
        Log.i("VBR-TSActivity", String.format("Update %s team captain", mTeamType.toString()));
        mIndoorTeamService.setCaptain(teamType, number);
        mCaptainButton.setText(String.valueOf(number));
        UiUtils.styleBaseIndoorTeamButton(getContext(), mIndoorTeamService, mTeamType, number, mCaptainButton);
    }

    private void selectCaptain() {
        Log.i("VBR-TSActivity", String.format("Select %s team captain", mTeamType.toString()));
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
        private       int           mColor;

        private LiberoAdapter(Context context, int color) {
            mContext = context;
            mColor = color;
        }

        @Override
        public int getCount() {
            return mIndoorTeamService.getPlayers(mTeamType).size();
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
            final List<Integer> players = new ArrayList<>(mIndoorTeamService.getPlayers(mTeamType));
            final int playerShirtNumber = players.get(position);
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
                    UiUtils.animate(mContext, button);
                    final int number = Integer.parseInt(button.getText().toString());
                    if (isChecked) {
                        if (mIndoorTeamService.canAddLibero(mTeamType)) {
                            Log.i("VBR-TSActivity", String.format("Checked #%d player of %s team as libero", number, mTeamType.toString()));
                            mIndoorTeamService.addLibero(mTeamType, number);
                            updateCaptain();
                        } else {
                            button.setChecked(false);
                        }
                    } else {
                        Log.i("VBR-TSActivity", String.format("Unchecked #%d player of %s team as libero", number, mTeamType.toString()));
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

    private void updateGender(GenderType genderType) {
        Context context = getContext();
        mIndoorTeamService.setGenderType(mTeamType, genderType);
        switch (genderType) {
            case MIXED:
                mGenderButton.setImageResource(R.drawable.ic_mixed);
                mGenderButton.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                mGenderButton.setImageResource(R.drawable.ic_ladies);
                mGenderButton.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                mGenderButton.setImageResource(R.drawable.ic_gents);
                mGenderButton.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorGents), PorterDuff.Mode.SRC_IN));
                break;
        }
    }

    private void computeConfirmItemVisibility() {
        if (getActivity() instanceof TeamsSetupActivity) {
            ((TeamsSetupActivity) getActivity()).computeConfirmItemVisibility();
        } else if (getActivity() instanceof SavedTeamActivity) {
            ((SavedTeamActivity) getActivity()).computeSaveItemVisibility();
        }
    }

}
