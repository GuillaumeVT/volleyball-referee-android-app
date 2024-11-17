package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.*;
import android.util.Log;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.*;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.ui.data.team.StoredTeamActivity;
import com.tonkar.volleyballreferee.ui.interfaces.BaseTeamServiceHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;

public class QuickTeamSetupFragment extends Fragment implements BaseTeamServiceHandler {

    private TeamType       mTeamType;
    private IBaseTeam      mTeamService;
    private MaterialButton mTeamColorButton;
    private MaterialButton mCaptainButton;
    private MaterialButton mGenderButton;
    private MaterialButton mPlayerNamesButton;

    public QuickTeamSetupFragment() {}

    public static QuickTeamSetupFragment newInstance(TeamType teamType) {
        QuickTeamSetupFragment fragment = new QuickTeamSetupFragment();
        Bundle args = new Bundle();
        args.putString(TeamType.class.getName(), teamType.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SETUP_UI, "Create team setup fragment");
        View view = inflater.inflate(R.layout.fragment_quick_team_setup, container, false);

        final String teamTypeStr = requireArguments().getString(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        final TextInputEditText teamNameInput = view.findViewById(R.id.team_name_input_text);
        final TextInputLayout teamNameInputLayout = view.findViewById(R.id.team_name_input_layout);
        mTeamColorButton = view.findViewById(R.id.team_color_button);
        mPlayerNamesButton = view.findViewById(R.id.team_player_names_button);
        mPlayerNamesButton.setOnClickListener(v -> showPlayerNamesInputDialogFragment());

        switch (mTeamType) {
            case HOME -> teamNameInputLayout.setHint(getString(R.string.home_team_hint));
            case GUEST -> teamNameInputLayout.setHint(getString(R.string.guest_team_hint));
        }

        final String teamName = mTeamService.getTeamName(mTeamType);
        teamNameInput.setText(teamName);
        teamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAdded()) {
                    Log.i(Tags.SETUP_UI, String.format("Update %s team name", mTeamType));
                    mTeamService.setTeamName(mTeamType, s.toString().trim());
                    ((TextInputLayout) view.findViewById(R.id.team_name_input_layout)).setError(
                            mTeamService.getTeamName(mTeamType).length() < IBaseTeam.TEAM_NAME_MIN_LENGTH ? String.format(
                                    Locale.getDefault(), getString(R.string.must_provide_at_least_n_characters), 2) : null);
                    computeSaveItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mCaptainButton = view.findViewById(R.id.team_captain_number_button);
        updateCaptain();
        mCaptainButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mCaptainButton);
            switchCaptain();
        });

        if (mTeamService.getTeamColor(mTeamType) == Color.parseColor(TeamDefinition.DEFAULT_COLOR)) {
            teamColorSelected(UiUtils.getRandomShirtColor(requireContext()));
        } else {
            teamColorSelected(mTeamService.getTeamColor(mTeamType));
        }
        mTeamColorButton.setOnClickListener(button -> {
            UiUtils.animate(getContext(), mTeamColorButton);
            selectTeamColor();
        });

        mGenderButton = view.findViewById(R.id.select_gender_button);
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
        UiUtils.colorTeamText(requireActivity(), color, mPlayerNamesButton);
        mTeamService.setTeamColor(mTeamType, color);
        updateCaptain();
    }

    private void updateCaptain() {
        int captain = mTeamService.getCaptain(mTeamType);
        captainUpdated(captain);
    }

    private void captainUpdated(int number) {
        Log.i(Tags.SETUP_UI, String.format("Update %s team captain", mTeamType));
        mTeamService.setCaptain(mTeamType, number);
        mCaptainButton.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleTeamButton(getContext(), mTeamService, mTeamType, number, mCaptainButton);
    }

    private void switchCaptain() {
        int captain = mTeamService.getCaptain(mTeamType);

        switch (captain) {
            case 1 -> captain = 2;
            case 2 -> captain = 1;
            default -> {
            }
        }

        captainUpdated(captain);
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

    private void computeSaveItemVisibility() {
        if (requireActivity() instanceof StoredTeamActivity) {
            ((StoredTeamActivity) requireActivity()).computeSaveLayoutVisibility();
        }
    }

    @Override
    public void setTeamService(IBaseTeam teamService) {
        mTeamService = teamService;
    }
}
