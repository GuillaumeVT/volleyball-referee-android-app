package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.TeamSummaryDto;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.team.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.*;

public class QuickGameSetupFragment extends Fragment implements GameServiceHandler {

    private IGame                       mGame;
    private MaterialButton              mGenderButton;
    private MaterialButton              mHomeTeamColorButton;
    private MaterialButton              mGuestTeamColorButton;
    private MaterialButton              mHomeTeamCaptainButton;
    private MaterialButton              mGuestTeamCaptainButton;
    private MaterialButton              mHomeTeamPlayerNamesButton;
    private MaterialButton              mGuestTeamPlayerNamesButton;
    private AutocompleteTeamListAdapter mHomeTeamAutocompleteTeamListAdapter;
    private AutocompleteTeamListAdapter mGuestTeamAutocompleteTeamListAdapter;

    public QuickGameSetupFragment() {}

    public static QuickGameSetupFragment newInstance(boolean create) {
        QuickGameSetupFragment fragment = new QuickGameSetupFragment();

        Bundle args = new Bundle();
        args.putBoolean("create", create);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SETUP_UI, "Create game setup fragment");
        View view = inflater.inflate(R.layout.fragment_quick_game_setup, container, false);

        final boolean create = requireArguments().getBoolean("create");

        final StoredTeamsService storedTeamsService = new StoredTeamsManager(getContext());

        mGenderButton = view.findViewById(R.id.switch_gender_button);
        updateGender(mGame.getGender());
        mGenderButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mGenderButton);
            GenderType genderType = mGame.getGender().next();
            updateGender(genderType);
        });
        mGenderButton.setEnabled(create);

        mHomeTeamCaptainButton = view.findViewById(R.id.home_team_captain_number_button);
        mGuestTeamCaptainButton = view.findViewById(R.id.guest_team_captain_number_button);

        final AutoCompleteTextView homeTeamNameInput = view.findViewById(R.id.home_team_name_input_text);
        homeTeamNameInput.setText(mGame.getTeamName(TeamType.HOME));
        homeTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAdded()) {
                    Log.i(Tags.SETUP_UI, String.format("Update %s team name", TeamType.HOME));
                    mGame.setTeamName(TeamType.HOME, s.toString().trim());
                    ((TextInputLayout) view.findViewById(R.id.home_team_name_input_layout)).setError(
                            mGame.getTeamName(TeamType.HOME).length() < IBaseTeam.TEAM_NAME_MIN_LENGTH ? String.format(Locale.getDefault(),
                                                                                                                       getString(
                                                                                                                               R.string.must_provide_at_least_n_characters),
                                                                                                                       IBaseTeam.TEAM_NAME_MIN_LENGTH) : null);
                    computeConfirmItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final AutoCompleteTextView guestTeamNameInput = view.findViewById(R.id.guest_team_name_input_text);
        guestTeamNameInput.setText(mGame.getTeamName(TeamType.GUEST));
        guestTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAdded()) {
                    Log.i(Tags.SETUP_UI, String.format("Update %s team name", TeamType.GUEST));
                    mGame.setTeamName(TeamType.GUEST, s.toString().trim());
                    ((TextInputLayout) view.findViewById(R.id.guest_team_name_input_layout)).setError(
                            mGame.getTeamName(TeamType.GUEST).length() < IBaseTeam.TEAM_NAME_MIN_LENGTH ? String.format(Locale.getDefault(),
                                                                                                                        getString(
                                                                                                                                R.string.must_provide_at_least_n_characters),
                                                                                                                        IBaseTeam.TEAM_NAME_MIN_LENGTH) : null);
                    computeConfirmItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mHomeTeamColorButton = view.findViewById(R.id.home_team_color_button);
        mHomeTeamColorButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mHomeTeamColorButton);
            selectTeamColor(TeamType.HOME);
        });
        mGuestTeamColorButton = view.findViewById(R.id.guest_team_color_button);
        mGuestTeamColorButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mGuestTeamColorButton);
            selectTeamColor(TeamType.GUEST);
        });

        mHomeTeamPlayerNamesButton = view.findViewById(R.id.home_team_player_names_button);
        mGuestTeamPlayerNamesButton = view.findViewById(R.id.guest_team_player_names_button);

        if (mGame.getTeamColor(TeamType.HOME) == Color.parseColor(TeamDefinition.DEFAULT_COLOR) && mGame.getTeamColor(
                TeamType.GUEST) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
            int homeTeamColor = UiUtils.getRandomShirtColor(requireContext());
            teamColorSelected(TeamType.HOME, homeTeamColor);

            boolean sameColor = true;
            int guestTeamColor = 0;

            while (sameColor) {
                guestTeamColor = UiUtils.getRandomShirtColor(requireContext());
                sameColor = (guestTeamColor == homeTeamColor);
            }
            teamColorSelected(TeamType.GUEST, guestTeamColor);
        } else {
            teamColorSelected(TeamType.HOME, mGame.getTeamColor(TeamType.HOME));
            teamColorSelected(TeamType.GUEST, mGame.getTeamColor(TeamType.GUEST));
        }

        if (GameType.BEACH.equals(mGame.getKind())) {
            updateCaptain(TeamType.HOME);
            mHomeTeamCaptainButton.setOnClickListener(button -> {
                UiUtils.animate(getContext(), mHomeTeamCaptainButton);
                switchCaptain(TeamType.HOME);
            });

            updateCaptain(TeamType.GUEST);
            mGuestTeamCaptainButton.setOnClickListener(button -> {
                UiUtils.animate(getContext(), mGuestTeamCaptainButton);
                switchCaptain(TeamType.GUEST);
            });

            List<TeamSummaryDto> teams = storedTeamsService.listTeams(GameType.BEACH);
            mHomeTeamAutocompleteTeamListAdapter = new AutocompleteTeamListAdapter(getContext(), getLayoutInflater(), teams);
            mGuestTeamAutocompleteTeamListAdapter = new AutocompleteTeamListAdapter(getContext(), getLayoutInflater(), teams);

            homeTeamNameInput.setAdapter(mHomeTeamAutocompleteTeamListAdapter);
            homeTeamNameInput.setThreshold(1);
            homeTeamNameInput.setOnItemClickListener((parent, input, index, id) -> {
                TeamSummaryDto teamDescription = mHomeTeamAutocompleteTeamListAdapter.getItem(index);
                homeTeamNameInput.setText(teamDescription.getName());
                storedTeamsService.copyTeam(storedTeamsService.getTeam(teamDescription.getId()), mGame, TeamType.HOME);

                teamColorSelected(TeamType.HOME, mGame.getTeamColor(TeamType.HOME));
                updateGender(mGame.getGender(TeamType.HOME));
                updateCaptain(TeamType.HOME);
                computeConfirmItemVisibility();
            });

            guestTeamNameInput.setAdapter(mGuestTeamAutocompleteTeamListAdapter);
            guestTeamNameInput.setThreshold(1);
            guestTeamNameInput.setOnItemClickListener((parent, input, index, id) -> {
                TeamSummaryDto teamDescription = mGuestTeamAutocompleteTeamListAdapter.getItem(index);
                guestTeamNameInput.setText(teamDescription.getName());
                storedTeamsService.copyTeam(storedTeamsService.getTeam(teamDescription.getId()), mGame, TeamType.GUEST);

                teamColorSelected(TeamType.GUEST, mGame.getTeamColor(TeamType.GUEST));
                updateGender(mGame.getGender(TeamType.GUEST));
                updateCaptain(TeamType.GUEST);
                computeConfirmItemVisibility();
            });

            mHomeTeamPlayerNamesButton.setOnClickListener(v -> showPlayerNamesInputDialogFragment(TeamType.HOME));
            mGuestTeamPlayerNamesButton.setOnClickListener(v -> showPlayerNamesInputDialogFragment(TeamType.GUEST));
        } else {
            View teamCaptainLayout = view.findViewById(R.id.home_team_captain_layout);
            teamCaptainLayout.setVisibility(View.GONE);
            teamCaptainLayout = view.findViewById(R.id.guest_team_captain_layout);
            teamCaptainLayout.setVisibility(View.GONE);
            View teamPlayerNamesLayout = view.findViewById(R.id.home_team_player_names_layout);
            teamPlayerNamesLayout.setVisibility(View.GONE);
            teamPlayerNamesLayout = view.findViewById(R.id.guest_team_player_names_layout);
            teamPlayerNamesLayout.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getChildFragmentManager().addFragmentOnAttachListener((fragmentManager, childFragment) -> {
            if (childFragment instanceof PlayerNamesInputDialogFragment fragment) {
                fragment.setTeam(mGame);
            }
        });
    }

    private void selectTeamColor(final TeamType teamType) {
        Log.i(Tags.SETUP_UI, String.format("Select %s team color", teamType));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), getContext(),
                                                                             getString(R.string.select_shirts_color),
                                                                             getResources().getStringArray(R.array.shirt_colors),
                                                                             mGame.getTeamColor(teamType)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(teamType, selectedColor);
                UiUtils.animateBounce(getContext(), TeamType.HOME.equals(teamType) ? mHomeTeamColorButton : mGuestTeamColorButton);
            }
        };
        colorSelectionDialog.show();
    }

    private void teamColorSelected(TeamType teamType, int colorId) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team color", teamType));
        final MaterialButton colorButton;
        final MaterialButton namesButton;

        if (TeamType.HOME.equals(teamType)) {
            colorButton = mHomeTeamColorButton;
            namesButton = mHomeTeamPlayerNamesButton;
        } else {
            colorButton = mGuestTeamColorButton;
            namesButton = mGuestTeamPlayerNamesButton;
        }

        UiUtils.colorTeamButton(getContext(), colorId, colorButton);
        UiUtils.colorTeamButton(getContext(), colorId, namesButton);
        mGame.setTeamColor(teamType, colorId);
        updateCaptain(teamType);
    }

    private void updateGender(GenderType genderType) {
        Context context = requireContext();
        mGame.setGender(genderType);
        switch (genderType) {
            case MIXED -> UiUtils.colorTeamButton(context, ContextCompat.getColor(context, R.color.colorMixed), R.drawable.ic_mixed,
                                                  mGenderButton);
            case LADIES -> UiUtils.colorTeamButton(context, ContextCompat.getColor(context, R.color.colorLadies), R.drawable.ic_ladies,
                                                   mGenderButton);
            case GENTS -> UiUtils.colorTeamButton(context, ContextCompat.getColor(context, R.color.colorGents), R.drawable.ic_gents,
                                                  mGenderButton);
        }
    }

    private void updateCaptain(TeamType teamType) {
        int captain = mGame.getCaptain(teamType);
        captainUpdated(teamType, captain);
    }

    private void captainUpdated(TeamType teamType, int number) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team captain", teamType));
        mGame.setCaptain(teamType, number);

        final MaterialButton button;

        if (TeamType.HOME.equals(teamType)) {
            button = mHomeTeamCaptainButton;
        } else {
            button = mGuestTeamCaptainButton;
        }

        button.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleTeamButton(getContext(), mGame, teamType, number, button);
    }

    private void switchCaptain(TeamType teamType) {
        int captain = mGame.getCaptain(teamType);

        switch (captain) {
            case 1 -> captain = 2;
            case 2 -> captain = 1;
            default -> {
            }
        }

        captainUpdated(teamType, captain);
    }

    private void computeConfirmItemVisibility() {
        if (requireActivity() instanceof QuickGameSetupActivity) {
            ((QuickGameSetupActivity) requireActivity()).computeStartGameButton();
        }
    }

    private void showPlayerNamesInputDialogFragment(TeamType teamType) {
        PlayerNamesInputDialogFragment fragment = PlayerNamesInputDialogFragment.newInstance(teamType);
        fragment.show(getChildFragmentManager(), "player_names_input_dialog");
        fragment.setTeam(mGame);
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
    }
}
