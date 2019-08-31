package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.team.GenderType;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.ui.interfaces.BaseTeamServiceHandler;
import com.tonkar.volleyballreferee.ui.stored.team.StoredTeamActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;

public class QuickTeamSetupFragment extends Fragment implements BaseTeamServiceHandler {

    private TeamType             mTeamType;
    private IBaseTeam            mTeamService;
    private FloatingActionButton mTeamColorButton;
    private MaterialButton       mCaptainButton;
    private FloatingActionButton mGenderButton;
    private FloatingActionButton mPlayerNamesButton;

    public QuickTeamSetupFragment() {}

    public static QuickTeamSetupFragment newInstance(TeamType teamType, boolean create) {
        QuickTeamSetupFragment fragment = new QuickTeamSetupFragment();
        Bundle args = new Bundle();
        args.putString(TeamType.class.getName(), teamType.toString());
        args.putBoolean("create", create);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SETUP_UI, "Create team setup fragment");
        View view = inflater.inflate(R.layout.fragment_quick_team_setup, container, false);

        final String teamTypeStr = getArguments().getString(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        final boolean create = getArguments().getBoolean("create");

        final TextInputEditText teamNameInput = view.findViewById(R.id.team_name_input_text);
        final TextInputLayout teamNameInputLayout = view.findViewById(R.id.team_name_input_layout);
        mTeamColorButton = view.findViewById(R.id.team_color_button);
        mPlayerNamesButton = view.findViewById(R.id.team_player_names_button);
        mPlayerNamesButton.setOnClickListener(v -> showPlayerNamesInputDialogFragment());

        switch (mTeamType) {
            case HOME:
                teamNameInputLayout.setHint(getString(R.string.home_team_hint));
                break;
            case GUEST:
                teamNameInputLayout.setHint(getString(R.string.guest_team_hint));
                break;
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
                    computeSaveItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final String teamName = mTeamService.getTeamName(mTeamType);

        teamNameInput.setText(teamName);
        teamNameInput.setEnabled(create);

        mCaptainButton = view.findViewById(R.id.team_captain_number_button);
        updateCaptain();
        mCaptainButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mCaptainButton);
            switchCaptain();
        });

        if (mTeamService.getTeamColor(mTeamType) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
            teamColorSelected(UiUtils.getRandomShirtColor(getContext()));
        } else {
            teamColorSelected(mTeamService.getTeamColor(mTeamType));
        }
        mTeamColorButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mTeamColorButton);
            selectTeamColor();
        });

        mGenderButton = view.findViewById(R.id.select_gender_button);
        mGenderButton.setEnabled(create);
        updateGender(mTeamService.getGender(mTeamType));
        mGenderButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mGenderButton);
            GenderType genderType = mTeamService.getGender(mTeamType).next();
            updateGender(genderType);
        });

        computeSaveItemVisibility();

        return view;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        if (childFragment instanceof PlayerNamesInputDialogFragment) {
            PlayerNamesInputDialogFragment fragment = (PlayerNamesInputDialogFragment) childFragment;
            fragment.setTeam(mTeamService);
        }
    }

    private void selectTeamColor() {
        Log.i(Tags.SETUP_UI, String.format("Select %s team color", mTeamType.toString()));
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), getContext(), getString(R.string.select_shirts_color),
                getResources().getStringArray(R.array.shirt_colors), mTeamService.getTeamColor(mTeamType)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(selectedColor);
                UiUtils.animateBounce(getContext(), mTeamColorButton);
            }
        };
        colorSelectionDialog.show();
    }

    private void teamColorSelected(int color) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team color", mTeamType.toString()));
        UiUtils.colorTeamIconButton(getActivity(), color, mTeamColorButton);
        UiUtils.colorTeamIconButton(getActivity(), color, mPlayerNamesButton);
        mTeamService.setTeamColor(mTeamType, color);
        updateCaptain();
    }

    private void updateCaptain() {
        int captain = mTeamService.getCaptain(mTeamType);
        captainUpdated(captain);
    }

    private void captainUpdated(int number) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team captain", mTeamType.toString()));
        mTeamService.setCaptain(mTeamType, number);
        mCaptainButton.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleTeamButton(getContext(), mTeamService, mTeamType, number, mCaptainButton);
    }

    private void switchCaptain() {
        int captain = mTeamService.getCaptain(mTeamType);

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

        captainUpdated(captain);
    }

    private void updateGender(GenderType genderType) {
        Context context = getContext();
        mTeamService.setGender(mTeamType, genderType);
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

    private void showPlayerNamesInputDialogFragment() {
        PlayerNamesInputDialogFragment fragment = PlayerNamesInputDialogFragment.newInstance(mTeamType);
        fragment.show(getChildFragmentManager(), "player_names_input_dialog");
        fragment.setTeam(mTeamService);
    }

    private void computeSaveItemVisibility() {
        if (getActivity() instanceof StoredTeamActivity) {
            ((StoredTeamActivity) getActivity()).computeSaveLayoutVisibility();
        }
    }

    @Override
    public void setTeamService(IBaseTeam teamService) {
        mTeamService = teamService;
    }
}
