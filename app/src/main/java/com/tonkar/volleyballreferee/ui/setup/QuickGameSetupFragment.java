package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.game.ITimeBasedGame;
import com.tonkar.volleyballreferee.engine.stored.*;
import com.tonkar.volleyballreferee.engine.stored.api.ApiFriend;
import com.tonkar.volleyballreferee.engine.stored.api.ApiLeagueSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTeamSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.team.GenderType;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.team.ColorSelectionDialog;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuickGameSetupFragment extends Fragment implements GameServiceHandler {

    private IGame                       mGame;
    private FloatingActionButton        mGenderButton;
    private FloatingActionButton        mHomeTeamColorButton;
    private FloatingActionButton        mGuestTeamColorButton;
    private MaterialButton              mHomeTeamCaptainButton;
    private MaterialButton              mGuestTeamCaptainButton;
    private AutocompleteTeamListAdapter mAutocompleteTeamListAdapter;

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

        final boolean create = getArguments().getBoolean("create");

        final StoredTeamsService storedTeamsService = new StoredTeamsManager(getContext());
        final StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(getContext());
        final StoredUserService storedUserService = new StoredUserManager(getContext());

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

        final AutoCompleteTextView divisionNameInput = view.findViewById(R.id.division_name_input_text);
        divisionNameInput.setThreshold(2);
        divisionNameInput.setOnItemClickListener((parent, input, index, id) -> {
            mGame.getLeague().setDivision((String) divisionNameInput.getAdapter().getItem(index));
            computeConfirmItemVisibility();
        });
        divisionNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAdded()) {
                    Log.i(Tags.SETUP_UI, "Update division");
                    mGame.getLeague().setDivision(s.toString().trim());
                    ((TextInputLayout) view.findViewById(R.id.division_name_input_layout)).setError(mGame.getLeague().getDivision().length() < 2 ? String.format(Locale.getDefault(), getString(R.string.must_provide_at_least_n_characters), 2) : null);
                    computeConfirmItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        divisionNameInput.setText(mGame.getLeague().getDivision());

        final AutoCompleteTextView leagueNameInput = view.findViewById(R.id.league_name_input_text);
        leagueNameInput.setThreshold(2);
        leagueNameInput.setAdapter(new AutocompleteLeagueListAdapter(getContext(), getLayoutInflater(), storedLeaguesService.listLeagues(mGame.getKind())));
        leagueNameInput.setOnItemClickListener((parent, input, index, id) -> {
            ApiLeagueSummary leagueDescription = (ApiLeagueSummary) leagueNameInput.getAdapter().getItem(index);
            leagueNameInput.setText(leagueDescription.getName());
            mGame.getLeague().setAll(leagueDescription);
            divisionNameInput.setText("");
            divisionNameInput.setAdapter(new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, new ArrayList<>(storedLeaguesService.listDivisionNames(leagueDescription.getId()))));
            computeConfirmItemVisibility();
        });

        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAdded()) {
                    Log.i(Tags.SETUP_UI, "Update league");
                    mGame.getLeague().setName(s.toString().trim());
                    view.findViewById(R.id.division_name_input_layout).setVisibility(count == 0 ? View.GONE : View.VISIBLE);
                    computeConfirmItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        leagueNameInput.setText(mGame.getLeague().getName());

        List<ApiFriend> referees = storedUserService.listReferees();

        Spinner refereeSpinner = view.findViewById(R.id.referee_spinner);
        if (PrefUtils.canSync(getContext())) {
            NameSpinnerAdapter<ApiFriend> refereeAdapter = new NameSpinnerAdapter<ApiFriend>(getContext(), inflater, referees) {
                @Override
                public String getName(ApiFriend referee) {
                    return referee.getPseudo();
                }

                @Override
                public String getId(ApiFriend referee) {
                    return referee.getId();
                }
            };
            refereeSpinner.setAdapter(refereeAdapter);
            if (refereeAdapter.getCount() > 0) {
                refereeSpinner.setSelection(refereeAdapter.getPositionFromId(mGame.getRefereedBy()));
            }
            refereeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(Tags.SETUP_UI, "Update referee");
                    ApiFriend referee = refereeAdapter.getItem(position);
                    mGame.setRefereedBy(referee.getId());
                    mGame.setRefereeName(referee.getPseudo());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mGame.setRefereedBy(ApiUserSummary.VBR_USER_ID);
                    mGame.setRefereeName(ApiUserSummary.VBR_PSEUDO);
                }
            });
        } else {
            view.findViewById(R.id.referee_spinner_title).setVisibility(View.GONE);
            refereeSpinner.setVisibility(View.GONE);
        }

        final AutoCompleteTextView homeTeamNameInput = view.findViewById(R.id.home_team_name_input_text);
        homeTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAdded()) {
                    Log.i(Tags.SETUP_UI, String.format("Update %s team name", TeamType.HOME.toString()));
                    mGame.setTeamName(TeamType.HOME, s.toString().trim());
                    ((TextInputLayout) view.findViewById(R.id.home_team_name_input_layout)).setError(mGame.getTeamName(TeamType.HOME).length() < 2 ? String.format(Locale.getDefault(), getString(R.string.must_provide_at_least_n_characters), 2) : null);
                    computeConfirmItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        homeTeamNameInput.setEnabled(create);

        final AutoCompleteTextView guestTeamNameInput = view.findViewById(R.id.guest_team_name_input_text);
        guestTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAdded()) {
                    Log.i(Tags.SETUP_UI, String.format("Update %s team name", TeamType.GUEST.toString()));
                    mGame.setTeamName(TeamType.GUEST, s.toString().trim());
                    ((TextInputLayout) view.findViewById(R.id.guest_team_name_input_layout)).setError(mGame.getTeamName(TeamType.GUEST).length() < 2 ? String.format(Locale.getDefault(), getString(R.string.must_provide_at_least_n_characters), 2) : null);
                    computeConfirmItemVisibility();
                }
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

        homeTeamNameInput.setText(mGame.getTeamName(TeamType.HOME));
        guestTeamNameInput.setText(mGame.getTeamName(TeamType.GUEST));

        if (mGame.getTeamColor(TeamType.HOME) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)
                && mGame.getTeamColor(TeamType.GUEST) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
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
            teamColorSelected(TeamType.HOME, mGame.getTeamColor(TeamType.HOME));
            teamColorSelected(TeamType.GUEST, mGame.getTeamColor(TeamType.GUEST));
        }

        NumberPicker matchDurationPicker = view.findViewById(R.id.match_duration_picker);
        TextView matchDurationText = view.findViewById(R.id.match_duration_text);

        if (GameType.TIME.equals(mGame.getKind())) {
            final ITimeBasedGame timeBasedGameService = (ITimeBasedGame) mGame;
            matchDurationPicker.setWrapSelectorWheel(false);
            matchDurationPicker.setMinValue(10);
            matchDurationPicker.setMaxValue(40);
            matchDurationPicker.setValue((int) (timeBasedGameService.getDuration() / 60000L));

            matchDurationPicker.setOnValueChangedListener((picker, oldValue, newValue) -> timeBasedGameService.setDuration(newValue * 60000L));
        } else {
            matchDurationPicker.setVisibility(View.GONE);
            matchDurationText.setVisibility(View.GONE);
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

            mAutocompleteTeamListAdapter = new AutocompleteTeamListAdapter(getContext(), getLayoutInflater(), storedTeamsService.listTeams(GameType.BEACH));

            homeTeamNameInput.setAdapter(mAutocompleteTeamListAdapter);
            homeTeamNameInput.setThreshold(2);
            homeTeamNameInput.setOnItemClickListener((parent, input, index, id) -> {
                ApiTeamSummary teamDescription = mAutocompleteTeamListAdapter.getItem(index);
                homeTeamNameInput.setText(teamDescription.getName());
                storedTeamsService.copyTeam(storedTeamsService.getTeam(teamDescription.getId()), mGame, TeamType.HOME);

                teamColorSelected(TeamType.HOME, mGame.getTeamColor(TeamType.HOME));
                updateGender(mGame.getGender(TeamType.HOME));
                updateCaptain(TeamType.HOME);
                computeConfirmItemVisibility();
            });

            guestTeamNameInput.setAdapter(mAutocompleteTeamListAdapter);
            guestTeamNameInput.setThreshold(2);
            guestTeamNameInput.setOnItemClickListener((parent, input, index, id) -> {
                ApiTeamSummary teamDescription = mAutocompleteTeamListAdapter.getItem(index);
                guestTeamNameInput.setText(teamDescription.getName());
                storedTeamsService.copyTeam(storedTeamsService.getTeam(teamDescription.getId()), mGame, TeamType.GUEST);

                teamColorSelected(TeamType.GUEST, mGame.getTeamColor(TeamType.GUEST));
                updateGender(mGame.getGender(TeamType.GUEST));
                updateCaptain(TeamType.GUEST);
                computeConfirmItemVisibility();
            });

            guestTeamNameInput.setAdapter(mAutocompleteTeamListAdapter);
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
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), getContext(), getString(R.string.select_shirts_color),
                getResources().getStringArray(R.array.shirt_colors), mGame.getTeamColor(teamType)) {
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
        mGame.setTeamColor(teamType, colorId);
        updateCaptain(teamType);
    }

    private void updateGender(GenderType genderType) {
        Context context = getContext();
        mGame.setGender(genderType);
        UiUtils.colorIconButtonInWhite(mGenderButton);
        switch (genderType) {
            case MIXED:
                UiUtils.colorTeamIconButton(context, getResources().getColor(R.color.colorMixed), R.drawable.ic_mixed, mGenderButton);
                break;
            case LADIES:
                UiUtils.colorTeamIconButton(context, getResources().getColor(R.color.colorLadies), R.drawable.ic_ladies, mGenderButton);
                break;
            case GENTS:
                UiUtils.colorTeamIconButton(context, getResources().getColor(R.color.colorGents), R.drawable.ic_gents, mGenderButton);
                break;
        }
    }

    private void updateCaptain(TeamType teamType) {
        int captain = mGame.getCaptain(teamType);
        captainUpdated(teamType, captain);
    }

    private void captainUpdated(TeamType teamType, int number) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team captain", teamType.toString()));
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
            ((QuickGameSetupActivity) getActivity()).computeStartLayoutVisibility();
        }
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
    }
}
