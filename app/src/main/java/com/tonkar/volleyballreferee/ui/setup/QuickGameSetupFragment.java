package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.RecordedGames;
import com.tonkar.volleyballreferee.business.data.RecordedTeam;
import com.tonkar.volleyballreferee.business.data.SavedTeams;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.TimeBasedGameService;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.data.SavedTeamsService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.util.ClearableTextInputAutoCompleteTextView;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import com.tonkar.volleyballreferee.ui.team.ColorSelectionDialog;

import java.util.ArrayList;
import java.util.List;

public class QuickGameSetupFragment extends Fragment implements GameServiceHandler {

    private GameService          mGameService;
    private FloatingActionButton mGenderButton;
    private FloatingActionButton mHomeTeamColorButton;
    private FloatingActionButton mGuestTeamColorButton;
    private MaterialButton       mHomeTeamCaptainButton;
    private MaterialButton       mGuestTeamCaptainButton;
    private TeamsListAdapter     mTeamsListAdapter;

    public QuickGameSetupFragment() {}

    public static QuickGameSetupFragment newInstance(boolean create) {
        QuickGameSetupFragment fragment = new QuickGameSetupFragment();

        Bundle args = new Bundle();
        args.putBoolean("create", create);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SETUP_UI, "Create game setup fragment");
        View view = inflater.inflate(R.layout.fragment_quick_game_setup, container, false);

        final boolean create = getArguments().getBoolean("create");

        final RecordedGamesService recordedGamesService = new RecordedGames(getContext());
        final SavedTeamsService savedTeamsService = new SavedTeams(getContext());

        mGenderButton = view.findViewById(R.id.switch_gender_button);
        updateGender(mGameService.getGenderType());
        mGenderButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mGenderButton);
            GenderType genderType = mGameService.getGenderType().next();
            updateGender(genderType);
        });
        mGenderButton.setEnabled(create);

        mHomeTeamCaptainButton = view.findViewById(R.id.home_team_captain_number_button);
        mGuestTeamCaptainButton = view.findViewById(R.id.guest_team_captain_number_button);

        final ClearableTextInputAutoCompleteTextView leagueNameInput = view.findViewById(R.id.league_name_input_text);
        leagueNameInput.setThreshold(2);
        List<String> leagueNames = new ArrayList<>(recordedGamesService.getRecordedLeagues());
        ArrayAdapter<String> leagueNameAdapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, leagueNames);
        leagueNameInput.setAdapter(leagueNameAdapter);
        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update league name");
                mGameService.setLeagueName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final ClearableTextInputAutoCompleteTextView divisionNameInput = view.findViewById(R.id.division_name_input_text);
        divisionNameInput.setThreshold(2);
        ArrayAdapter<String> divisionNameAdapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, new ArrayList<>(recordedGamesService.getRecordedDivisions()));
        divisionNameInput.setAdapter(divisionNameAdapter);
        divisionNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update division name");
                mGameService.setDivisionName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        divisionNameInput.setText(mGameService.getDivisionName());

        final ClearableTextInputAutoCompleteTextView homeTeamNameInput = view.findViewById(R.id.home_team_name_input_text);
        homeTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, String.format("Update %s team name", TeamType.HOME.toString()));
                mGameService.setTeamName(TeamType.HOME, s.toString());
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        homeTeamNameInput.setEnabled(create);

        final ClearableTextInputAutoCompleteTextView guestTeamNameInput = view.findViewById(R.id.guest_team_name_input_text);
        guestTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, String.format("Update %s team name", TeamType.GUEST.toString()));
                mGameService.setTeamName(TeamType.GUEST, s.toString());
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        guestTeamNameInput.setEnabled(create);

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

        leagueNameInput.setText(mGameService.getLeagueName());
        homeTeamNameInput.setText(mGameService.getTeamName(TeamType.HOME));
        guestTeamNameInput.setText(mGameService.getTeamName(TeamType.GUEST));

        if (mGameService.getTeamColor(TeamType.HOME) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)
                && mGameService.getTeamColor(TeamType.GUEST) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
            int homeTeamColor = UiUtils.getRandomShirtColor(getContext());
            teamColorSelected(TeamType.HOME, homeTeamColor);

            boolean sameColor = true;
            int guestTeamColor = 0;

            while (sameColor) {
                guestTeamColor = UiUtils.getRandomShirtColor(getContext());
                sameColor = (guestTeamColor == homeTeamColor);
            }
            teamColorSelected(TeamType.GUEST, guestTeamColor);
        } else {
            teamColorSelected(TeamType.HOME, mGameService.getTeamColor(TeamType.HOME));
            teamColorSelected(TeamType.GUEST, mGameService.getTeamColor(TeamType.GUEST));
        }

        NumberPicker matchDurationPicker = view.findViewById(R.id.match_duration_picker);
        TextView matchDurationText = view.findViewById(R.id.match_duration_text);

        if (GameType.TIME.equals(mGameService.getGameType())) {
            final TimeBasedGameService timeBasedGameService = (TimeBasedGameService) mGameService;
            matchDurationPicker.setWrapSelectorWheel(false);
            matchDurationPicker.setMinValue(10);
            matchDurationPicker.setMaxValue(40);
            matchDurationPicker.setValue((int) (timeBasedGameService.getDuration() / 60000L));

            matchDurationPicker.setOnValueChangedListener((picker, oldValue, newValue) -> timeBasedGameService.setDuration(newValue * 60000L));
        } else {
            matchDurationPicker.setVisibility(View.GONE);
            matchDurationText.setVisibility(View.GONE);
        }

        if (GameType.BEACH.equals(mGameService.getGameType())) {
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

            mTeamsListAdapter = new TeamsListAdapter(getContext(), getLayoutInflater(), savedTeamsService.getSavedTeamList(GameType.BEACH));

            homeTeamNameInput.setAdapter(mTeamsListAdapter);
            homeTeamNameInput.setThreshold(2);
            homeTeamNameInput.setOnItemClickListener((parent, input, index, id) -> {
                RecordedTeam team = mTeamsListAdapter.getItem(index);
                homeTeamNameInput.setText(team.getName());
                savedTeamsService.copyTeam(team, mGameService, TeamType.HOME);

                teamColorSelected(TeamType.HOME, mGameService.getTeamColor(TeamType.HOME));
                updateGender(mGameService.getGenderType(TeamType.HOME));
                updateCaptain(TeamType.HOME);
                computeConfirmItemVisibility();
            });

            guestTeamNameInput.setAdapter(mTeamsListAdapter);
            guestTeamNameInput.setThreshold(2);
            guestTeamNameInput.setOnItemClickListener((parent, input, index, id) -> {
                RecordedTeam team = mTeamsListAdapter.getItem(index);
                guestTeamNameInput.setText(team.getName());
                savedTeamsService.copyTeam(team, mGameService, TeamType.GUEST);

                teamColorSelected(TeamType.GUEST, mGameService.getTeamColor(TeamType.GUEST));
                updateGender(mGameService.getGenderType(TeamType.GUEST));
                updateCaptain(TeamType.GUEST);
                computeConfirmItemVisibility();
            });

            guestTeamNameInput.setAdapter(mTeamsListAdapter);
            guestTeamNameInput.setThreshold(2);
        } else {
            View teamCaptainLayout = view.findViewById(R.id.home_team_captain_layout);
            teamCaptainLayout.setVisibility(View.GONE);
            teamCaptainLayout = view.findViewById(R.id.guest_team_captain_layout);
            teamCaptainLayout.setVisibility(View.GONE);
        }

        return view;
    }

    private void selectTeamColor(final TeamType teamType) {
        Log.i(Tags.SETUP_UI, String.format("Select %s team color", teamType.toString()));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), getContext(), getResources().getString(R.string.select_shirts_color),
                getResources().getStringArray(R.array.shirt_colors), mGameService.getTeamColor(teamType)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(teamType, selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    private void teamColorSelected(TeamType teamType, int colorId) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team color", teamType.toString()));
        final FloatingActionButton button;

        if (TeamType.HOME.equals(teamType)) {
            button = mHomeTeamColorButton;
        } else {
            button = mGuestTeamColorButton;
        }

        UiUtils.colorTeamIconButton(getContext(), colorId, button);
        mGameService.setTeamColor(teamType, colorId);
        updateCaptain(teamType);
    }

    private void updateGender(GenderType genderType) {
        Context context = getContext();
        mGameService.setGenderType(genderType);
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

    private void updateCaptain(TeamType teamType) {
        int captain = mGameService.getCaptain(teamType);
        captainUpdated(teamType, captain);
    }

    private void captainUpdated(TeamType teamType, int number) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team captain", teamType.toString()));
        mGameService.setCaptain(teamType, number);

        final MaterialButton button;

        if (TeamType.HOME.equals(teamType)) {
            button = mHomeTeamCaptainButton;
        } else {
            button = mGuestTeamCaptainButton;
        }

        button.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleTeamButton(getContext(), mGameService, teamType, number, button);
    }

    private void switchCaptain(TeamType teamType) {
        int captain = mGameService.getCaptain(teamType);

        switch (captain) {
            case 1:
                captain = 2;
                break;
            case 2:
                captain = 1;
                break;
            default:
                break;
        }

        captainUpdated(teamType, captain);
    }

    private void computeConfirmItemVisibility() {
        if (getActivity() instanceof QuickGameSetupActivity) {
            ((QuickGameSetupActivity) getActivity()).computeStartItemVisibility();
        }
    }

    @Override
    public void setGameService(GameService gameService) {
        mGameService = gameService;
    }
}
