package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.RecordedTeam;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.TimeBasedGameService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.TextInputAutoCompleteTextView;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.team.ColorSelectionDialog;

import java.util.ArrayList;
import java.util.List;

public class QuickGameSetupFragment extends Fragment {

    private ImageButton      mGenderButton;
    private ImageButton      mHomeTeamColorButton;
    private ImageButton      mGuestTeamColorButton;
    private Button           mHomeTeamCaptainButton;
    private Button           mGuestTeamCaptainButton;
    private TeamsListAdapter mTeamsListAdapter;

    public QuickGameSetupFragment() {
    }

    public static QuickGameSetupFragment newInstance() {
        return new QuickGameSetupFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-QGSActivity", "Create game setup fragment");
        View view = inflater.inflate(R.layout.fragment_quick_game_setup, container, false);

        if (ServicesProvider.getInstance().areSetupServicesUnavailable()) {
            ServicesProvider.getInstance().restoreGameServiceForSetup(getActivity().getApplicationContext());
        }

        mGenderButton = view.findViewById(R.id.switch_gender_button);
        updateGender(ServicesProvider.getInstance().getTeamService().getGenderType());
        mGenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.animate(getContext(), mGenderButton);
                GenderType genderType = ServicesProvider.getInstance().getTeamService().getGenderType().next();
                updateGender(genderType);
            }
        });

        mHomeTeamCaptainButton = view.findViewById(R.id.home_team_captain_number_button);
        mGuestTeamCaptainButton = view.findViewById(R.id.guest_team_captain_number_button);

        final TextInputAutoCompleteTextView leagueNameInput = view.findViewById(R.id.league_name_input_text);
        leagueNameInput.setThreshold(2);
        List<String> leagueNames = new ArrayList<>(ServicesProvider.getInstance().getRecordedGamesService().getRecordedLeagues());
        ArrayAdapter<String> leagueNameAdapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, leagueNames);
        leagueNameInput.setAdapter(leagueNameAdapter);
        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-QGSActivity", "Update league name");
                ServicesProvider.getInstance().getGeneralService().setLeagueName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final TextInputAutoCompleteTextView divisionNameInput = view.findViewById(R.id.division_name_input_text);
        divisionNameInput.setThreshold(2);
        ArrayAdapter<String> divisionNameAdapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, new ArrayList<>(ServicesProvider.getInstance().getRecordedGamesService().getRecordedDivisions()));
        divisionNameInput.setAdapter(divisionNameAdapter);
        divisionNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-QGSActivity", "Update division name");
                ServicesProvider.getInstance().getGeneralService().setDivisionName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        divisionNameInput.setText(ServicesProvider.getInstance().getGeneralService().getDivisionName());

        final TextInputAutoCompleteTextView homeTeamNameInput = view.findViewById(R.id.home_team_name_input_text);
        homeTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-QGSActivity", String.format("Update %s team name", TeamType.HOME.toString()));
                ServicesProvider.getInstance().getTeamService().setTeamName(TeamType.HOME, s.toString());
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final TextInputAutoCompleteTextView guestTeamNameInput = view.findViewById(R.id.guest_team_name_input_text);
        guestTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-QGSActivity", String.format("Update %s team name", TeamType.GUEST.toString()));
                ServicesProvider.getInstance().getTeamService().setTeamName(TeamType.GUEST, s.toString());
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mHomeTeamColorButton = view.findViewById(R.id.home_team_color_button);
        mHomeTeamColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.animate(getContext(), mHomeTeamColorButton);
                selectTeamColor(TeamType.HOME);
            }
        });
        mGuestTeamColorButton = view.findViewById(R.id.guest_team_color_button);
        mGuestTeamColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.animate(getContext(), mGuestTeamColorButton);
                selectTeamColor(TeamType.GUEST);
            }
        });

        leagueNameInput.setText(ServicesProvider.getInstance().getGeneralService().getLeagueName());
        homeTeamNameInput.setText(ServicesProvider.getInstance().getTeamService().getTeamName(TeamType.HOME));
        guestTeamNameInput.setText(ServicesProvider.getInstance().getTeamService().getTeamName(TeamType.GUEST));

        if (ServicesProvider.getInstance().getTeamService().getTeamColor(TeamType.HOME) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)
                && ServicesProvider.getInstance().getTeamService().getTeamColor(TeamType.GUEST) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
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
            teamColorSelected(TeamType.HOME, ServicesProvider.getInstance().getTeamService().getTeamColor(TeamType.HOME));
            teamColorSelected(TeamType.GUEST, ServicesProvider.getInstance().getTeamService().getTeamColor(TeamType.GUEST));
        }

        NumberPicker matchDurationPicker = view.findViewById(R.id.match_duration_picker);
        TextView matchDurationText = view.findViewById(R.id.match_duration_text);

        if (GameType.TIME.equals(ServicesProvider.getInstance().getGeneralService().getGameType())) {
            final TimeBasedGameService timeBasedGameService = (TimeBasedGameService) ServicesProvider.getInstance().getGameService();
            matchDurationPicker.setWrapSelectorWheel(false);
            matchDurationPicker.setMinValue(10);
            matchDurationPicker.setMaxValue(40);
            matchDurationPicker.setValue((int) (timeBasedGameService.getDuration() / 60000L));

            matchDurationPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldValue, int newValue){
                    timeBasedGameService.setDuration(newValue * 60000L);
                }
            });
        } else {
            matchDurationPicker.setVisibility(View.GONE);
            matchDurationText.setVisibility(View.GONE);
        }

        if (GameType.BEACH.equals(ServicesProvider.getInstance().getGeneralService().getGameType())) {
            updateCaptain(TeamType.HOME);
            mHomeTeamCaptainButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UiUtils.animate(getContext(), mHomeTeamCaptainButton);
                    switchCaptain(TeamType.HOME);
                }
            });

            updateCaptain(TeamType.GUEST);
            mGuestTeamCaptainButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UiUtils.animate(getContext(), mGuestTeamCaptainButton);
                    switchCaptain(TeamType.GUEST);
                }
            });

            mTeamsListAdapter = new TeamsListAdapter(getContext(), getLayoutInflater(), ServicesProvider.getInstance().getSavedTeamsService().getSavedTeamList(GameType.BEACH));

            homeTeamNameInput.setAdapter(mTeamsListAdapter);
            homeTeamNameInput.setThreshold(2);
            homeTeamNameInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                    RecordedTeam team = mTeamsListAdapter.getItem(index);
                    homeTeamNameInput.setText(team.getName());
                    ServicesProvider.getInstance().getSavedTeamsService().copyTeam(team, ServicesProvider.getInstance().getTeamService(), TeamType.HOME);

                    teamColorSelected(TeamType.HOME, ServicesProvider.getInstance().getTeamService().getTeamColor(TeamType.HOME));
                    updateGender(ServicesProvider.getInstance().getTeamService().getGenderType(TeamType.HOME));
                    updateCaptain(TeamType.HOME);
                    computeConfirmItemVisibility();
                }
            });

            guestTeamNameInput.setAdapter(mTeamsListAdapter);
            guestTeamNameInput.setThreshold(2);
            guestTeamNameInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                    RecordedTeam team = mTeamsListAdapter.getItem(index);
                    guestTeamNameInput.setText(team.getName());
                    ServicesProvider.getInstance().getSavedTeamsService().copyTeam(team, ServicesProvider.getInstance().getTeamService(), TeamType.GUEST);

                    teamColorSelected(TeamType.GUEST, ServicesProvider.getInstance().getTeamService().getTeamColor(TeamType.GUEST));
                    updateGender(ServicesProvider.getInstance().getTeamService().getGenderType(TeamType.GUEST));
                    updateCaptain(TeamType.GUEST);
                    computeConfirmItemVisibility();
                }
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
        Log.i("VBR-QGSActivity", String.format("Select %s team color", teamType.toString()));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), getContext(), getResources().getString(R.string.select_shirts_color),
                getResources().getStringArray(R.array.shirt_colors), ServicesProvider.getInstance().getTeamService().getTeamColor(teamType)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(teamType, selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    private void teamColorSelected(TeamType teamType, int colorId) {
        Log.i("VBR-QGSActivity", String.format("Update %s team color", teamType.toString()));
        final ImageButton button;

        if (TeamType.HOME.equals(teamType)) {
            button = mHomeTeamColorButton;
        } else {
            button = mGuestTeamColorButton;
        }

        UiUtils.colorTeamIconButton(getContext(), colorId, button);
        ServicesProvider.getInstance().getTeamService().setTeamColor(teamType, colorId);
        updateCaptain(teamType);
    }

    private void updateGender(GenderType genderType) {
        Context context = getContext();
        ServicesProvider.getInstance().getTeamService().setGenderType(genderType);
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
        int captain = ServicesProvider.getInstance().getTeamService().getCaptain(teamType);
        captainUpdated(teamType, captain);
    }

    private void captainUpdated(TeamType teamType, int number) {
        Log.i("VBR-QGSActivity", String.format("Update %s team captain", teamType.toString()));
        ServicesProvider.getInstance().getTeamService().setCaptain(teamType, number);

        final Button button;

        if (TeamType.HOME.equals(teamType)) {
            button = mHomeTeamCaptainButton;
        } else {
            button = mGuestTeamCaptainButton;
        }

        button.setText(String.valueOf(number));
        UiUtils.styleTeamButton(getContext(), ServicesProvider.getInstance().getTeamService(), teamType, number, button);
    }

    private void switchCaptain(TeamType teamType) {
        int captain = ServicesProvider.getInstance().getTeamService().getCaptain(teamType);

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
            ((QuickGameSetupActivity) getActivity()).computeConfirmItemVisibility();
        }
    }
}
