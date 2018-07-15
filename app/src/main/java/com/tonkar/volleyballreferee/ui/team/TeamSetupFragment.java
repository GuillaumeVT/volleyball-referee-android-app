package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.RecordedTeam;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.TextInputAutoCompleteTextView;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.data.SavedTeamActivity;
import com.tonkar.volleyballreferee.ui.setup.GameSetupActivity;
import com.tonkar.volleyballreferee.ui.setup.TeamsListAdapter;

import java.util.ArrayList;
import java.util.List;

public class TeamSetupFragment extends Fragment {

    private LayoutInflater  mLayoutInflater;
    private TeamType        mTeamType;
    private BaseTeamService mTeamService;
    private ImageButton     mTeamColorButton;
    private PlayerAdapter   mPlayerAdapter;
    private ImageButton     mLiberoColorButton;
    private Button          mCaptainButton;
    private LiberoAdapter   mLiberoAdapter;
    private ImageButton     mGenderButton;
    private ScrollView      mScrollView;

    public TeamSetupFragment() {
    }

    public static TeamSetupFragment newInstance(TeamType teamType) {
        return newInstance(teamType, true, true);
    }

    public static TeamSetupFragment newInstance(TeamType teamType, boolean isGameContext, boolean editable) {
        TeamSetupFragment fragment = new TeamSetupFragment();
        Bundle args = new Bundle();
        args.putString(TeamType.class.getName(), teamType.toString());
        args.putBoolean("is_game", isGameContext);
        args.putBoolean("editable", editable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-TSActivity", "Create team setup fragment");
        mLayoutInflater = inflater;
        View view = mLayoutInflater.inflate(R.layout.fragment_team_setup, container, false);

        final String teamTypeStr = getArguments().getString(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        final boolean editable = getArguments().getBoolean("editable");

        final boolean isGameContext = getArguments().getBoolean("is_game");

        if (isGameContext) {
            if (ServicesProvider.getInstance().areSetupServicesUnavailable()) {
                ServicesProvider.getInstance().restoreGameServiceForSetup(getActivity().getApplicationContext());
            }
            mTeamService = ServicesProvider.getInstance().getTeamService();
        } else {
            if (ServicesProvider.getInstance().isSavedTeamsServiceUnavailable()) {
                ServicesProvider.getInstance().restoreSavedTeamsService(getActivity().getApplicationContext());
            }
            mTeamService = ServicesProvider.getInstance().getSavedTeamsService().getCurrentTeam();
        }

        mScrollView = view.findViewById(R.id.team_setup_scroll);

        final TextInputAutoCompleteTextView teamNameInput = view.findViewById(R.id.team_name_input_text);
        final TextInputLayout teamNameInputLayout = view.findViewById(R.id.team_name_input_layout);
        mTeamColorButton = view.findViewById(R.id.team_color_button);
        final GridView teamNumbersGrid = view.findViewById(R.id.team_member_numbers_grid);
        mCaptainButton = view.findViewById(R.id.team_captain_number_button);
        final GridView liberoNumbersGrid = view.findViewById(R.id.team_libero_numbers_grid);
        mLiberoColorButton = view.findViewById(R.id.libero_color_button);

        switch (mTeamType) {
            case HOME:
                teamNameInputLayout.setHint(getResources().getString(R.string.home_team_hint));
                break;
            case GUEST:
                teamNameInputLayout.setHint(getResources().getString(R.string.guest_team_hint));
                break;
        }

        final String teamName = mTeamService.getTeamName(mTeamType);

        teamNameInput.setText(teamName);
        teamNameInput.setEnabled(editable);

        if (isGameContext) {
            teamNameInput.setThreshold(2);
            teamNameInput.setAdapter(new TeamsListAdapter(getContext(), getLayoutInflater(), ServicesProvider.getInstance().getSavedTeamsService().getSavedTeamList(mTeamService.getTeamsKind())));
            teamNameInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                    RecordedTeam team = (RecordedTeam) teamNameInput.getAdapter().getItem(index);
                    teamNameInput.setText(team.getName());
                    ServicesProvider.getInstance().getSavedTeamsService().copyTeam(team, mTeamService, mTeamType);

                    teamColorSelected(mTeamService.getTeamColor(mTeamType));
                    updateGender(mTeamService.getGenderType(mTeamType));
                    mPlayerAdapter.notifyDataSetChanged();
                    captainUpdated(mTeamType, mTeamService.getCaptain(mTeamType));
                    if (manageLiberos()) {
                        liberoColorSelected(mTeamService.getLiberoColor(mTeamType));
                        mLiberoAdapter.notifyDataSetChanged();
                    }
                    mScrollView.post(new Runnable() {
                        public void run() {
                            mScrollView.fullScroll(ScrollView.FOCUS_UP);
                        }
                    });
                    computeConfirmItemVisibility();
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
                mTeamService.setTeamName(mTeamType, s.toString());
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mPlayerAdapter = new PlayerAdapter(getActivity(), mTeamService.getTeamColor(mTeamType));
        teamNumbersGrid.setAdapter(mPlayerAdapter);

        if (mTeamService.getTeamColor(mTeamType) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
            teamColorSelected(UiUtils.getRandomShirtColor(getActivity()));
        } else {
            teamColorSelected(mTeamService.getTeamColor(mTeamType));
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

        if (manageLiberos()) {
            mLiberoAdapter = new LiberoAdapter(getActivity(), mTeamService.getLiberoColor(mTeamType));
            liberoNumbersGrid.setAdapter(mLiberoAdapter);

            if (mTeamService.getLiberoColor(mTeamType) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
                liberoColorSelected(UiUtils.getRandomShirtColor(getActivity()));
            } else {
                liberoColorSelected(mTeamService.getLiberoColor(mTeamType));
            }
            mLiberoColorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UiUtils.animate(getContext(), mLiberoColorButton);
                    selectLiberoColor();
                }
            });
        } else {
            liberoNumbersGrid.setVisibility(View.GONE);
            mLiberoColorButton.setVisibility(View.GONE);
            final TextView liberoNumbersTitle =  view.findViewById(R.id.team_libero_numbers_title);
            liberoNumbersTitle.setVisibility(View.GONE);
        }

        mGenderButton = view.findViewById(R.id.select_gender_button);
        mGenderButton.setEnabled(editable);
        updateGender(mTeamService.getGenderType(mTeamType));
        mGenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.animate(getContext(), mGenderButton);
                GenderType genderType = mTeamService.getGenderType(mTeamType).next();
                updateGender(genderType);
            }
        });

        computeConfirmItemVisibility();

        return view;
    }

    private void selectTeamColor() {
        Log.i("VBR-TSActivity", String.format("Select %s team color", mTeamType.toString()));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), getContext(), getResources().getString(R.string.select_shirts_color),
                getResources().getStringArray(R.array.shirt_colors), mTeamService.getTeamColor(mTeamType)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    private void teamColorSelected(int color) {
        Log.i("VBR-TSActivity", String.format("Update %s team color", mTeamType.toString()));
        UiUtils.colorTeamIconButton(getActivity(), color, mTeamColorButton);
        mTeamService.setTeamColor(mTeamType, color);
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
            button.setChecked(mTeamService.hasPlayer(mTeamType, playerShirtNumber));
            button.setColor(mContext, mColor);

            button.setOnCheckedChangeListener(new PlayerToggleButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(PlayerToggleButton button, boolean isChecked) {
                    UiUtils.animate(mContext, button);
                    final int number = Integer.parseInt(button.getText().toString());
                    if (isChecked) {
                        Log.i("VBR-TSActivity", String.format("Checked #%d player of %s team", number, mTeamType.toString()));
                        mTeamService.addPlayer(mTeamType, number);
                    } else {
                        Log.i("VBR-TSActivity", String.format("Unchecked #%d player of %s team", number, mTeamType.toString()));
                        mTeamService.removePlayer(mTeamType, number);
                    }
                    updateCaptain();
                    if (manageLiberos()) {
                        mLiberoAdapter.notifyDataSetChanged();
                    }
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
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(mLayoutInflater, getContext(), getResources().getString(R.string.select_shirts_color),
                getResources().getStringArray(R.array.shirt_colors), mTeamService.getLiberoColor(mTeamType)) {
            @Override
            public void onColorSelected(int selectedColor) {
                liberoColorSelected(selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    private void liberoColorSelected(int color) {
        Log.i("VBR-TSActivity", String.format("Update %s team libero color", mTeamType.toString()));
        UiUtils.colorTeamIconButton(getActivity(), color, mLiberoColorButton);
        mTeamService.setLiberoColor(mTeamType, color);
        mLiberoAdapter.setColor(color);
    }

    private void updateCaptain() {
        int captain = mTeamService.getCaptain(mTeamType);
        if ((captain < 1 || mTeamService.isLibero(mTeamType, captain)) && !mTeamService.getPossibleCaptains(mTeamType).isEmpty()) {
            captain = mTeamService.getPossibleCaptains(mTeamType).iterator().next();
        }

        captainUpdated(mTeamType, captain);
    }

    private void captainUpdated(TeamType teamType, int number) {
        Log.i("VBR-TSActivity", String.format("Update %s team captain", teamType.toString()));
        mTeamService.setCaptain(teamType, number);
        mCaptainButton.setText(String.valueOf(number));
        UiUtils.styleTeamButton(getContext(), mTeamService, teamType, number, mCaptainButton);
    }

    private void selectCaptain() {
        Log.i("VBR-TSActivity", String.format("Select %s team captain", mTeamType.toString()));
        IndoorPlayerSelectionDialog playerSelectionDialog = new IndoorPlayerSelectionDialog(mLayoutInflater, getContext(), getResources().getString(R.string.select_captain), mTeamService,
                mTeamType, mTeamService.getPossibleCaptains(mTeamType)) {
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
            return mTeamService.getPlayers(mTeamType).size();
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
            final List<Integer> players = new ArrayList<>(mTeamService.getPlayers(mTeamType));
            final int playerShirtNumber = players.get(position);
            final PlayerToggleButton button;

            if (view == null) {
                button = new PlayerToggleButton(mContext);
            } else {
                button = (PlayerToggleButton) view;
            }

            button.setText(String.valueOf(playerShirtNumber));
            button.setChecked(mTeamService.isLibero(mTeamType, playerShirtNumber));
            button.setColor(mContext, mColor);

            button.setOnCheckedChangeListener(new PlayerToggleButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(PlayerToggleButton button, boolean isChecked) {
                    UiUtils.animate(mContext, button);
                    final int number = Integer.parseInt(button.getText().toString());
                    if (isChecked) {
                        if (mTeamService.canAddLibero(mTeamType)) {
                            Log.i("VBR-TSActivity", String.format("Checked #%d player of %s team as libero", number, mTeamType.toString()));
                            mTeamService.addLibero(mTeamType, number);
                            updateCaptain();
                        } else {
                            button.setChecked(false);
                        }
                    } else {
                        Log.i("VBR-TSActivity", String.format("Unchecked #%d player of %s team as libero", number, mTeamType.toString()));
                        mTeamService.removeLibero(mTeamType, number);
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
        mTeamService.setGenderType(mTeamType, genderType);
        UiUtils.colorIconButtonInWhite(context, mGenderButton);
        switch (genderType) {
            case MIXED:
                mGenderButton.setImageResource(R.drawable.ic_mixed);
                mGenderButton.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                mGenderButton.setImageResource(R.drawable.ic_ladies);
                mGenderButton.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                mGenderButton.setImageResource(R.drawable.ic_gents);
                mGenderButton.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorGents), PorterDuff.Mode.SRC_IN));
                break;
        }
    }

    private void computeConfirmItemVisibility() {
        if (getActivity() instanceof GameSetupActivity) {
            ((GameSetupActivity) getActivity()).computeConfirmItemVisibility();
        } else if (getActivity() instanceof SavedTeamActivity) {
            ((SavedTeamActivity) getActivity()).computeSaveItemVisibility();
        }
    }

    private boolean manageLiberos() {
        return mTeamService.getExpectedNumberOfPlayersOnCourt() == 6;
    }
}
