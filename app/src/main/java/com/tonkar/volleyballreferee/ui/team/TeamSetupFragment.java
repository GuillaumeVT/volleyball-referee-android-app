package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiPlayer;
import com.tonkar.volleyballreferee.api.ApiTeamDescription;
import com.tonkar.volleyballreferee.business.data.StoredTeams;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.StoredTeamsService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.BaseTeamServiceHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import com.tonkar.volleyballreferee.ui.data.StoredTeamActivity;
import com.tonkar.volleyballreferee.ui.setup.GameSetupActivity;
import com.tonkar.volleyballreferee.ui.setup.AutocompleteTeamListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeamSetupFragment extends Fragment implements BaseTeamServiceHandler {

    private LayoutInflater       mLayoutInflater;
    private TeamType             mTeamType;
    private BaseTeamService      mTeamService;
    private int                  mNumberOfShirts;
    private FloatingActionButton mTeamColorButton;
    private PlayerAdapter        mPlayerAdapter;
    private FloatingActionButton mLiberoColorButton;
    private MaterialButton       mCaptainButton;
    private LiberoAdapter        mLiberoAdapter;
    private FloatingActionButton mGenderButton;
    private ScrollView           mScrollView;

    public TeamSetupFragment() {}

    public static TeamSetupFragment newInstance(TeamType teamType, boolean isGameContext, boolean create) {
        TeamSetupFragment fragment = new TeamSetupFragment();

        Bundle args = new Bundle();
        args.putString(TeamType.class.getName(), teamType.toString());
        args.putBoolean("is_game", isGameContext);
        args.putBoolean("create", create);
        args.putInt("number_of_shirts", 26);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SETUP_UI, "Create team setup fragment");
        mLayoutInflater = inflater;
        View view = mLayoutInflater.inflate(R.layout.fragment_team_setup, container, false);

        final String teamTypeStr = getArguments().getString(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        final boolean create = getArguments().getBoolean("create");
        final boolean isGameContext = getArguments().getBoolean("is_game");

        if (savedInstanceState == null) {
            mNumberOfShirts = getArguments().getInt("number_of_shirts");
        } else {
            mNumberOfShirts = savedInstanceState.getInt("number_of_shirts");
        }

        mScrollView = view.findViewById(R.id.team_setup_scroll);

        final AutoCompleteTextView teamNameInput = view.findViewById(R.id.team_name_input_text);
        final TextInputLayout teamNameInputLayout = view.findViewById(R.id.team_name_input_layout);
        mTeamColorButton = view.findViewById(R.id.team_color_button);
        final GridView teamNumbersGrid = view.findViewById(R.id.team_member_numbers_grid);
        mCaptainButton = view.findViewById(R.id.team_captain_number_button);
        final GridView liberoNumbersGrid = view.findViewById(R.id.team_libero_numbers_grid);
        mLiberoColorButton = view.findViewById(R.id.libero_color_button);

        switch (mTeamType) {
            case HOME:
                teamNameInputLayout.setHint(getString(R.string.home_team_hint));
                break;
            case GUEST:
                teamNameInputLayout.setHint(getString(R.string.guest_team_hint));
                break;
        }

        final String teamName = mTeamService.getTeamName(mTeamType);

        if (isGameContext) {
            StoredTeamsService storedTeamsService = new StoredTeams(getContext());

            teamNameInput.setThreshold(2);
            teamNameInput.setAdapter(new AutocompleteTeamListAdapter(getContext(), getLayoutInflater(), storedTeamsService.listTeams(mTeamService.getTeamsKind())));
            teamNameInput.setOnItemClickListener((parent, input, index, id) -> {
                ApiTeamDescription teamDescription = (ApiTeamDescription) teamNameInput.getAdapter().getItem(index);
                teamNameInput.setText(teamDescription.getName());
                storedTeamsService.copyTeam(storedTeamsService.getTeam(teamDescription.getId()), mTeamService, mTeamType);

                teamColorSelected(mTeamService.getTeamColor(mTeamType));
                updateGender(mTeamService.getGender(mTeamType));
                mPlayerAdapter.notifyDataSetChanged();
                captainUpdated(mTeamType, mTeamService.getCaptain(mTeamType));
                if (manageLiberos()) {
                    liberoColorSelected(mTeamService.getLiberoColor(mTeamType));
                    mLiberoAdapter.notifyDataSetChanged();
                }
                mScrollView.post(() -> mScrollView.fullScroll(ScrollView.FOCUS_UP));
                computeConfirmItemVisibility();
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
                if (isAdded()) {
                    Log.i(Tags.SETUP_UI, String.format("Update %s team name", mTeamType.toString()));
                    mTeamService.setTeamName(mTeamType, s.toString().trim());
                    ((TextInputLayout) view.findViewById(R.id.team_name_input_layout)).setError(mTeamService.getTeamName(mTeamType).length() < 2 ? String.format(Locale.getDefault(), getString(R.string.must_provide_at_least_n_characters), 2) : null);
                    computeConfirmItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        teamNameInput.setText(teamName);
        teamNameInput.setEnabled(create);

        mPlayerAdapter = new PlayerAdapter(getLayoutInflater(), getActivity(), mTeamService.getTeamColor(mTeamType));
        teamNumbersGrid.setAdapter(mPlayerAdapter);

        if (mTeamService.getTeamColor(mTeamType) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
            teamColorSelected(UiUtils.getRandomShirtColor(getContext()));
        } else {
            teamColorSelected(mTeamService.getTeamColor(mTeamType));
        }
        mTeamColorButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mTeamColorButton);
            selectTeamColor();
        });

        updateCaptain();
        mCaptainButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mCaptainButton);
            selectCaptain();
        });

        if (manageLiberos()) {
            mLiberoAdapter = new LiberoAdapter(getLayoutInflater(), getActivity(), mTeamService.getLiberoColor(mTeamType));
            liberoNumbersGrid.setAdapter(mLiberoAdapter);

            if (mTeamService.getLiberoColor(mTeamType) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
                liberoColorSelected(UiUtils.getRandomShirtColor(getActivity()));
            } else {
                liberoColorSelected(mTeamService.getLiberoColor(mTeamType));
            }
            mLiberoColorButton.setOnClickListener(button -> {
                UiUtils.animate(getContext(), mLiberoColorButton);
                selectLiberoColor();
            });
        } else {
            liberoNumbersGrid.setVisibility(View.GONE);
            mLiberoColorButton.hide();
            final TextView liberoNumbersTitle =  view.findViewById(R.id.team_libero_numbers_title);
            liberoNumbersTitle.setVisibility(View.GONE);
        }

        mGenderButton = view.findViewById(R.id.select_gender_button);
        mGenderButton.setEnabled(create);
        updateGender(mTeamService.getGender(mTeamType));
        mGenderButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mGenderButton);
            GenderType genderType = mTeamService.getGender(mTeamType).next();
            updateGender(genderType);
        });

        computeConfirmItemVisibility();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("number_of_shirts", mNumberOfShirts);
    }

    private void selectTeamColor() {
        Log.i(Tags.SETUP_UI, String.format("Select %s team color", mTeamType.toString()));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), getContext(), getString(R.string.select_shirts_color),
                getResources().getStringArray(R.array.shirt_colors), mTeamService.getTeamColor(mTeamType)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    private void teamColorSelected(int color) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team color", mTeamType.toString()));
        UiUtils.colorTeamIconButton(getActivity(), color, mTeamColorButton);
        mTeamService.setTeamColor(mTeamType, color);
        mPlayerAdapter.setColor(color);
        updateCaptain();
    }

    @Override
    public void setTeamService(BaseTeamService teamService) {
        mTeamService = teamService;
    }

    private class PlayerAdapter extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private final Context        mContext;
        private       int            mColor;
        private       int            mCount;

        private PlayerAdapter(LayoutInflater layoutInflater, Context context, int color) {
            mLayoutInflater = layoutInflater;
            mContext = context;
            mColor = color;
            initCount();
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
            final int playerShirtNumber = position;
            PlayerToggleButton button;

            if (view == null) {
                button = (PlayerToggleButton) mLayoutInflater.inflate(R.layout.player_toggle_item, null);
            } else {
                button = (PlayerToggleButton) view;
            }

            final boolean isShirt = isShirt(position);
            final boolean isLess = isLess(position);

            if (isShirt) {
                button.setText(UiUtils.formatNumberFromLocale(playerShirtNumber));
                button.setChecked(mTeamService.hasPlayer(mTeamType, playerShirtNumber));
                button.setColor(mContext, mColor);
            } else if (isLess) {
                button.setText("-");
                button.setChecked(false);
            } else {
                button.setText("+");
                button.setChecked(false);
            }

            button.setOnCheckedChangeListener((cButton, isChecked) -> {
                UiUtils.animate(mContext, cButton);
                if (isShirt) {
                    final int number = Integer.parseInt(cButton.getText().toString());
                    if (isChecked) {
                        Log.i(Tags.SETUP_UI, String.format("Checked #%d player of %s team", number, mTeamType.toString()));
                        mTeamService.addPlayer(mTeamType, number);
                    } else {
                        Log.i(Tags.SETUP_UI, String.format("Unchecked #%d player of %s team", number, mTeamType.toString()));
                        mTeamService.removePlayer(mTeamType, number);
                    }
                    updateCaptain();
                    if (manageLiberos()) {
                        mLiberoAdapter.notifyDataSetChanged();
                    }
                    computeConfirmItemVisibility();
                } else if (isLess) {
                    lessShirts();
                    initCount();
                    notifyDataSetChanged();
                } else {
                    moreShirts();
                    initCount();
                    notifyDataSetChanged();
                }
            });

            return button;
        }

        public void setColor(int color) {
            mColor = color;
            notifyDataSetChanged();
        }

        private void initCount() {
            switch (mNumberOfShirts) {
                case 100:
                    mCount = 101;
                    break;
                case 51:
                    mCount = 53;
                    break;
                default:
                    mCount = 27;
                    break;
            }
        }

        private boolean isShirt(int position) {
            return position < mNumberOfShirts;
        }

        private boolean isLess(int position) {
            final boolean result;

            switch (mNumberOfShirts) {
                case 100:
                case 51:
                    result = position == mNumberOfShirts;
                    break;
                default:
                    result = false;
                    break;
            }

            return result;
        }

        private void moreShirts() {
            switch (mNumberOfShirts) {
                case 26:
                    mNumberOfShirts = 51;
                    break;
                case 51:
                    mNumberOfShirts = 100;
                    break;
            }
        }

        private void lessShirts() {
            switch (mNumberOfShirts) {
                case 100:
                    mNumberOfShirts = 51;
                    break;
                case 51:
                    mNumberOfShirts = 26;
                    break;
            }
        }
    }

    private void selectLiberoColor() {
        Log.i(Tags.SETUP_UI, String.format("Select %s team libero color", mTeamType.toString()));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(mLayoutInflater, getContext(), getString(R.string.select_shirts_color),
                getResources().getStringArray(R.array.shirt_colors), mTeamService.getLiberoColor(mTeamType)) {
            @Override
            public void onColorSelected(int selectedColor) {
                liberoColorSelected(selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    private void liberoColorSelected(int color) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team libero color", mTeamType.toString()));
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
        Log.i(Tags.SETUP_UI, String.format("Update %s team captain", teamType.toString()));
        mTeamService.setCaptain(teamType, number);
        mCaptainButton.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleTeamButton(getContext(), mTeamService, teamType, number, mCaptainButton);
    }

    private void selectCaptain() {
        Log.i(Tags.SETUP_UI, String.format("Select %s team captain", mTeamType.toString()));
        IndoorPlayerSelectionDialog playerSelectionDialog = new IndoorPlayerSelectionDialog(mLayoutInflater, getContext(), getString(R.string.select_captain), mTeamService,
                mTeamType, mTeamService.getPossibleCaptains(mTeamType)) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                captainUpdated(mTeamType, selectedNumber);
            }
        };
        playerSelectionDialog.show();
    }

    private class LiberoAdapter extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private final Context        mContext;
        private       int            mColor;

        private LiberoAdapter(LayoutInflater layoutInflater, Context context, int color) {
            mLayoutInflater = layoutInflater;
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
            final List<ApiPlayer> players = new ArrayList<>(mTeamService.getPlayers(mTeamType));
            final int playerShirtNumber = players.get(position).getNum();
            final PlayerToggleButton button;

            if (view == null) {
                button = (PlayerToggleButton) mLayoutInflater.inflate(R.layout.player_toggle_item, null);
            } else {
                button = (PlayerToggleButton) view;
            }

            button.setText(UiUtils.formatNumberFromLocale(playerShirtNumber));
            button.setChecked(mTeamService.isLibero(mTeamType, playerShirtNumber));
            button.setColor(mContext, mColor);

            button.setOnCheckedChangeListener((cButton, isChecked) -> {
                UiUtils.animate(mContext, cButton);
                final int number = Integer.parseInt(cButton.getText().toString());
                if (isChecked) {
                    if (mTeamService.canAddLibero(mTeamType)) {
                        Log.i(Tags.SETUP_UI, String.format("Checked #%d player of %s team as libero", number, mTeamType.toString()));
                        mTeamService.addLibero(mTeamType, number);
                        updateCaptain();
                    } else {
                        cButton.setChecked(false);
                    }
                } else {
                    Log.i(Tags.SETUP_UI, String.format("Unchecked #%d player of %s team as libero", number, mTeamType.toString()));
                    mTeamService.removeLibero(mTeamType, number);
                    updateCaptain();
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
        mTeamService.setGender(mTeamType, genderType);
        UiUtils.colorIconButtonInWhite(mGenderButton);
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
            ((GameSetupActivity) getActivity()).computeStartItemVisibility();
        } else if (getActivity() instanceof StoredTeamActivity) {
            ((StoredTeamActivity) getActivity()).computeSaveItemVisibility();
        }
    }

    private boolean manageLiberos() {
        return mTeamService.getExpectedNumberOfPlayersOnCourt() == 6;
    }
}
