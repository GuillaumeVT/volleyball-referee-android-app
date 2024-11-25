package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.ui.data.team.StoredTeamActivity;
import com.tonkar.volleyballreferee.ui.interfaces.BaseTeamServiceHandler;
import com.tonkar.volleyballreferee.ui.setup.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.*;

public class TeamSetupFragment extends Fragment implements BaseTeamServiceHandler {

    private LayoutInflater mLayoutInflater;
    private TeamType       mTeamType;
    private IBaseTeam      mTeamService;
    private int            mNumberOfShirts;
    private MaterialButton mTeamColorButton;
    private PlayerAdapter  mPlayerAdapter;
    private MaterialButton mLiberoColorButton;
    private MaterialButton mCaptainButton;
    private LiberoAdapter  mLiberoAdapter;
    private MaterialButton mGenderButton;
    private MaterialButton mPlayerNamesButton;

    public TeamSetupFragment() {}

    public static TeamSetupFragment newInstance(GameType gameType, TeamType teamType, boolean isGameContext) {
        TeamSetupFragment fragment = new TeamSetupFragment();

        Bundle args = new Bundle();
        args.putString(GameType.class.getName(), gameType.toString());
        args.putString(TeamType.class.getName(), teamType.toString());
        args.putBoolean("is_game", isGameContext);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SETUP_UI, "Create team setup fragment");
        mLayoutInflater = inflater;
        View fragmentView = mLayoutInflater.inflate(R.layout.fragment_team_setup, container, false);

        final String teamTypeStr = requireArguments().getString(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        final boolean isGameContext = requireArguments().getBoolean("is_game");

        if (savedInstanceState == null) {
            mNumberOfShirts = GameType.SNOW.equals(mTeamService.getTeamsKind()) ? 4 : 26;
        } else {
            mNumberOfShirts = savedInstanceState.getInt("number_of_shirts");
        }

        final AutoCompleteTextView teamNameInput = fragmentView.findViewById(R.id.team_name_input_text);
        final TextInputLayout teamNameInputLayout = fragmentView.findViewById(R.id.team_name_input_layout);
        mTeamColorButton = fragmentView.findViewById(R.id.team_color_button);
        final GridView teamNumbersGrid = fragmentView.findViewById(R.id.team_member_numbers_grid);
        mCaptainButton = fragmentView.findViewById(R.id.team_captain_number_button);
        final GridView liberoNumbersGrid = fragmentView.findViewById(R.id.team_libero_numbers_grid);
        mLiberoColorButton = fragmentView.findViewById(R.id.libero_color_button);
        mPlayerNamesButton = fragmentView.findViewById(R.id.team_player_names_button);
        mPlayerNamesButton.setOnClickListener(v -> showPlayerNamesInputDialogFragment());
        final EditText coachNameInput = fragmentView.findViewById(R.id.coach_name_input_text);
        final TextInputLayout coachNameInputLayout = fragmentView.findViewById(R.id.coach_name_input_layout);

        switch (mTeamType) {
            case HOME -> teamNameInputLayout.setHint(getString(R.string.home_team_hint));
            case GUEST -> teamNameInputLayout.setHint(getString(R.string.guest_team_hint));
        }

        if (isGameContext) {
            StoredTeamsService storedTeamsService = new StoredTeamsManager(getContext());

            teamNameInput.setThreshold(1);
            teamNameInput.setAdapter(new AutocompleteTeamListAdapter(getContext(), getLayoutInflater(),
                                                                     storedTeamsService.listTeams(mTeamService.getTeamsKind())));
            teamNameInput.setOnItemClickListener((parent, input, index, id) -> {
                TeamSummaryDto teamDescription = (TeamSummaryDto) teamNameInput.getAdapter().getItem(index);
                teamNameInput.setText(teamDescription.getName());
                storedTeamsService.copyTeam(storedTeamsService.getTeam(teamDescription.getId()), mTeamService, mTeamType);

                teamColorSelected(mTeamService.getTeamColor(mTeamType));
                updateGender(mTeamService.getGender(mTeamType));
                mPlayerAdapter.notifyDataSetChanged();
                captainUpdated(mTeamType, mTeamService.getCaptain(mTeamType));
                if (GameType.INDOOR.equals(mTeamService.getTeamsKind())) {
                    liberoColorSelected(mTeamService.getLiberoColor(mTeamType));
                    mLiberoAdapter.notifyDataSetChanged();
                }
                coachNameInput.setText(mTeamService.getCoachName(mTeamType));
                computeConfirmItemVisibility();
            });
        }

        final String teamName = mTeamService.getTeamName(mTeamType);

        if (!teamName.isEmpty()) {
            requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        teamNameInput.setText(teamName);
        teamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAdded()) {
                    Log.i(Tags.SETUP_UI, String.format("Update %s team name", mTeamType));
                    mTeamService.setTeamName(mTeamType, s.toString().trim());
                    ((TextInputLayout) fragmentView.findViewById(R.id.team_name_input_layout)).setError(
                            mTeamService.getTeamName(mTeamType).length() < IBaseTeam.TEAM_NAME_MIN_LENGTH ? String.format(
                                    Locale.getDefault(), getString(R.string.must_provide_at_least_n_characters),
                                    IBaseTeam.TEAM_NAME_MIN_LENGTH) : null);
                    computeConfirmItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mPlayerAdapter = new PlayerAdapter(getLayoutInflater(), requireActivity(), mTeamService.getTeamColor(mTeamType));
        teamNumbersGrid.setAdapter(mPlayerAdapter);

        if (mTeamService.getTeamColor(mTeamType) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
            teamColorSelected(UiUtils.getRandomShirtColor(requireContext()));
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

        if (GameType.INDOOR.equals(mTeamService.getTeamsKind())) {
            mLiberoAdapter = new LiberoAdapter(getLayoutInflater(), requireActivity(), mTeamService.getLiberoColor(mTeamType));
            liberoNumbersGrid.setAdapter(mLiberoAdapter);

            if (mTeamService.getLiberoColor(mTeamType) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
                liberoColorSelected(UiUtils.getRandomShirtColor(requireContext()));
            } else {
                liberoColorSelected(mTeamService.getLiberoColor(mTeamType));
            }
            mLiberoColorButton.setOnClickListener(button -> {
                UiUtils.animate(getContext(), mLiberoColorButton);
                selectLiberoColor();
            });

            TextView teamNumbersGridTitle = fragmentView.findViewById(R.id.team_member_numbers_title);
            teamNumbersGridTitle.setText(getString(R.string.select_players_with_liberos));
        } else {
            liberoNumbersGrid.setVisibility(View.GONE);
            mLiberoColorButton.setVisibility(View.GONE);
            final TextView liberoNumbersTitle = fragmentView.findViewById(R.id.team_libero_numbers_title);
            liberoNumbersTitle.setVisibility(View.GONE);
        }

        mGenderButton = fragmentView.findViewById(R.id.select_gender_button);
        updateGender(mTeamService.getGender(mTeamType));
        mGenderButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mGenderButton);
            GenderType genderType = mTeamService.getGender(mTeamType).next();
            updateGender(genderType);
        });

        if (GameType.INDOOR.equals(mTeamService.getTeamsKind()) || GameType.INDOOR_4X4.equals(mTeamService.getTeamsKind())) {
            coachNameInput.setText(mTeamService.getCoachName(mTeamType));
            coachNameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isAdded()) {
                        Log.i(Tags.SETUP_UI, String.format("Update %s coach name", mTeamType));
                        mTeamService.setCoachName(mTeamType, s.toString().trim());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        } else {
            coachNameInputLayout.setVisibility(View.GONE);
        }

        computeConfirmItemVisibility();

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("number_of_shirts", mNumberOfShirts);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getChildFragmentManager().addFragmentOnAttachListener((fragmentManager, childFragment) -> {
            if (childFragment instanceof PlayerNamesInputDialogFragment fragment) {
                fragment.setTeam(mTeamService);
            }
        });
    }

    private void selectTeamColor() {
        Log.i(Tags.SETUP_UI, String.format("Select %s team color", mTeamType));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), getContext(),
                                                                             getString(R.string.select_shirts_color),
                                                                             getResources().getStringArray(R.array.shirt_colors),
                                                                             mTeamService.getTeamColor(mTeamType)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(selectedColor);
                UiUtils.animateBounce(getContext(), mTeamColorButton);
            }
        };
        colorSelectionDialog.show();
    }

    private void teamColorSelected(int color) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team color", mTeamType));
        UiUtils.colorTeamButton(requireActivity(), color, mTeamColorButton);
        UiUtils.colorTeamButton(requireActivity(), color, mPlayerNamesButton);
        mTeamService.setTeamColor(mTeamType, color);
        mPlayerAdapter.setColor(color);
        updateCaptain();
    }

    @Override
    public void setTeamService(IBaseTeam teamService) {
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
            // Snow volley: start shirts at 1 instead of 0
            final int playerShirtNumber = GameType.SNOW.equals(mTeamService.getTeamsKind()) ? position + 1 : position;
            PlayerToggleButton button;

            if (view == null) {
                button = (PlayerToggleButton) mLayoutInflater.inflate(R.layout.player_toggle_item, parent, false);
            } else {
                button = (PlayerToggleButton) view;
            }

            button.clearOnCheckedChangeListeners();

            final boolean isShirt = isShirt(position);
            final boolean isLess = isLess(position);

            if (isShirt) {
                button.setText(UiUtils.formatNumberFromLocale(playerShirtNumber));
                button.setChecked(mTeamService.hasPlayer(mTeamType, playerShirtNumber));
            } else if (isLess) {
                button.setText("-");
                button.setChecked(false);
            } else {
                button.setText("+");
                button.setChecked(false);
            }

            button.setColor(mContext, mColor);

            // Snow volley: Cannot deselect players 1,2,3
            button.setEnabled(!GameType.SNOW.equals(mTeamService.getTeamsKind()) || playerShirtNumber == 4);

            button.addOnCheckedChangeListener((cButton, isChecked) -> {
                UiUtils.animate(mContext, cButton);
                if (isShirt) {
                    final int number = Integer.parseInt(cButton.getText().toString());
                    if (isChecked) {
                        Log.i(Tags.SETUP_UI, String.format("Checked #%d player of %s team", number, mTeamType));
                        mTeamService.addPlayer(mTeamType, number);
                    } else {
                        Log.i(Tags.SETUP_UI, String.format("Unchecked #%d player of %s team", number, mTeamType));
                        mTeamService.removePlayer(mTeamType, number);
                    }
                    updateCaptain();
                    if (GameType.INDOOR.equals(mTeamService.getTeamsKind())) {
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
                ((PlayerToggleButton) cButton).setColor(mContext, mColor);
            });

            return button;
        }

        public void setColor(int color) {
            mColor = color;
            notifyDataSetChanged();
        }

        private void initCount() {
            switch (mNumberOfShirts) {
                // Snow volley: at most 4 players
                case 4 -> mCount = 4;
                case 100 -> mCount = 101;
                case 51 -> mCount = 53;
                default -> mCount = 27;
            }
        }

        private boolean isShirt(int position) {
            return position < mNumberOfShirts;
        }

        private boolean isLess(int position) {
            return switch (mNumberOfShirts) {
                case 100, 51 -> position == mNumberOfShirts;
                default -> false;
            };
        }

        private void moreShirts() {
            switch (mNumberOfShirts) {
                case 26 -> mNumberOfShirts = 51;
                case 51 -> mNumberOfShirts = 100;
            }
        }

        private void lessShirts() {
            switch (mNumberOfShirts) {
                case 100 -> mNumberOfShirts = 51;
                case 51 -> mNumberOfShirts = 26;
            }
        }
    }

    private void selectLiberoColor() {
        Log.i(Tags.SETUP_UI, String.format("Select %s team libero color", mTeamType));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(mLayoutInflater, getContext(),
                                                                             getString(R.string.select_shirts_color),
                                                                             getResources().getStringArray(R.array.shirt_colors),
                                                                             mTeamService.getLiberoColor(mTeamType)) {
            @Override
            public void onColorSelected(int selectedColor) {
                liberoColorSelected(selectedColor);
                UiUtils.animateBounce(getContext(), mLiberoColorButton);
            }
        };
        colorSelectionDialog.show();
    }

    private void liberoColorSelected(int color) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team libero color", mTeamType));
        UiUtils.colorTeamButton(requireActivity(), color, mLiberoColorButton);
        mTeamService.setLiberoColor(mTeamType, color);
        mLiberoAdapter.setColor(color);
    }

    private void updateCaptain() {
        int captain = mTeamService.getCaptain(mTeamType);
        if (captain < 1 && !mTeamService.getPossibleCaptains(mTeamType).isEmpty()) {
            captain = mTeamService.getPossibleCaptains(mTeamType).iterator().next();
        }

        captainUpdated(mTeamType, captain);
    }

    private void captainUpdated(TeamType teamType, int number) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team captain", teamType));
        mTeamService.setCaptain(teamType, number);
        mCaptainButton.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleTeamButton(getContext(), mTeamService, teamType, number, mCaptainButton);
    }

    private void selectCaptain() {
        Log.i(Tags.SETUP_UI, String.format("Select %s team captain", mTeamType));
        PlayerSelectionDialog playerSelectionDialog = new PlayerSelectionDialog(mLayoutInflater, getContext(),
                                                                                getString(R.string.select_captain), mTeamService, mTeamType,
                                                                                mTeamService.getPossibleCaptains(mTeamType)) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                captainUpdated(mTeamType, selectedNumber);
                UiUtils.animateBounce(getContext(), mCaptainButton);
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
            final List<PlayerDto> players = new ArrayList<>(mTeamService.getPlayers(mTeamType));
            final int playerShirtNumber = players.get(position).getNum();
            final PlayerToggleButton button;

            if (view == null) {
                button = (PlayerToggleButton) mLayoutInflater.inflate(R.layout.player_toggle_item, parent, false);
            } else {
                button = (PlayerToggleButton) view;
            }

            button.clearOnCheckedChangeListeners();

            button.setText(UiUtils.formatNumberFromLocale(playerShirtNumber));
            button.setChecked(mTeamService.isLibero(mTeamType, playerShirtNumber));
            button.setColor(mContext, mColor);

            button.addOnCheckedChangeListener((cButton, isChecked) -> {
                UiUtils.animate(mContext, cButton);
                final int number = Integer.parseInt(cButton.getText().toString());
                if (isChecked) {
                    if (mTeamService.canAddLibero(mTeamType)) {
                        Log.i(Tags.SETUP_UI, String.format("Checked #%d player of %s team as libero", number, mTeamType));
                        mTeamService.addLibero(mTeamType, number);
                        updateCaptain();
                    } else {
                        cButton.setChecked(false);
                    }
                } else {
                    Log.i(Tags.SETUP_UI, String.format("Unchecked #%d player of %s team as libero", number, mTeamType));
                    mTeamService.removeLibero(mTeamType, number);
                    updateCaptain();
                }
                ((PlayerToggleButton) cButton).setColor(mContext, mColor);
            });

            return button;
        }

        public void setColor(int color) {
            mColor = color;
            notifyDataSetChanged();
        }
    }

    private void updateGender(GenderType genderType) {
        Context context = requireContext();
        mTeamService.setGender(mTeamType, genderType);
        switch (genderType) {
            case MIXED -> UiUtils.colorTeamButton(context, ContextCompat.getColor(context, R.color.colorMixed), R.drawable.ic_mixed,
                                                  mGenderButton);
            case LADIES -> UiUtils.colorTeamButton(context, ContextCompat.getColor(context, R.color.colorLadies), R.drawable.ic_ladies,
                                                   mGenderButton);
            case GENTS -> UiUtils.colorTeamButton(context, ContextCompat.getColor(context, R.color.colorGents), R.drawable.ic_gents,
                                                  mGenderButton);
        }
    }

    private void showPlayerNamesInputDialogFragment() {
        PlayerNamesInputDialogFragment fragment = PlayerNamesInputDialogFragment.newInstance(mTeamType);
        fragment.show(getChildFragmentManager(), "player_names_input_dialog");
        fragment.setTeam(mTeamService);
    }

    private void computeConfirmItemVisibility() {
        if (requireActivity() instanceof GameSetupActivity) {
            ((GameSetupActivity) requireActivity()).computeStartGameButton();
        } else if (requireActivity() instanceof StoredTeamActivity) {
            ((StoredTeamActivity) requireActivity()).computeSaveLayoutVisibility();
        }
    }
}
