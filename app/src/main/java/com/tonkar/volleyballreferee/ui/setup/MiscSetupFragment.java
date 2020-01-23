package com.tonkar.volleyballreferee.ui.setup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.stored.StoredLeaguesManager;
import com.tonkar.volleyballreferee.engine.stored.StoredLeaguesService;
import com.tonkar.volleyballreferee.engine.stored.StoredUserManager;
import com.tonkar.volleyballreferee.engine.stored.StoredUserService;
import com.tonkar.volleyballreferee.engine.stored.api.ApiFriend;
import com.tonkar.volleyballreferee.engine.stored.api.ApiLeagueSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MiscSetupFragment extends Fragment implements GameServiceHandler {

    private IGame mGame;

    public MiscSetupFragment() {}

    public static MiscSetupFragment newInstance() {
        MiscSetupFragment fragment = new MiscSetupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SETUP_UI, "Create misc setup fragment");
        View view = inflater.inflate(R.layout.fragment_misc_setup, container, false);

        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(getContext());
        StoredUserService storedUserService = new StoredUserManager(getContext());

        final AutoCompleteTextView divisionNameInput = view.findViewById(R.id.division_name_input_text);
        divisionNameInput.setText(mGame.getLeague().getDivision());
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

        final AutoCompleteTextView leagueNameInput = view.findViewById(R.id.league_name_input_text);
        leagueNameInput.setText(mGame.getLeague().getName());
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
                    view.findViewById(R.id.division_name_input_layout).setVisibility(mGame.getLeague().getName().length() < 2 ? View.GONE : View.VISIBLE);
                    computeConfirmItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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

        final TextInputLayout referee1InputLayout = view.findViewById(R.id.referee1_name_input_layout);
        final TextInputLayout referee2InputLayout = view.findViewById(R.id.referee2_name_input_layout);
        final TextInputLayout scorerInputLayout = view.findViewById(R.id.scorer_name_input_layout);

        if (PrefUtils.isScoreSheetsPurchased(getContext())) {
            final EditText referee1Input = view.findViewById(R.id.referee1_name_input_text);
            referee1Input.setText(mGame.getReferee1Name());
            referee1InputLayout.setHint(String.format(Locale.getDefault(), "%s %d", getString(R.string.referee), 1));
            referee1Input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isAdded()) {
                        Log.i(Tags.SETUP_UI, "Update referee 1");
                        mGame.setReferee1Name(s.toString().trim());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            final EditText referee2Input = view.findViewById(R.id.referee2_name_input_text);
            referee2Input.setText(mGame.getReferee2Name());
            referee2InputLayout.setHint(String.format(Locale.getDefault(), "%s %d", getString(R.string.referee), 2));
            referee2Input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isAdded()) {
                        Log.i(Tags.SETUP_UI, "Update referee 2");
                        mGame.setReferee2Name(s.toString().trim());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            final EditText scorerInput = view.findViewById(R.id.scorer_name_input_text);
            scorerInput.setText(mGame.getScorerName());
            scorerInputLayout.setHint(getString(R.string.scorer));
            scorerInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isAdded()) {
                        Log.i(Tags.SETUP_UI, "Update scorer");
                        mGame.setScorerName(s.toString().trim());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        } else {
            referee1InputLayout.setVisibility(View.GONE);
            referee2InputLayout.setVisibility(View.GONE);
            scorerInputLayout.setVisibility(View.GONE);
        }

        return view;
    }

    private void computeConfirmItemVisibility() {
        if (getActivity() instanceof GameSetupActivity) {
            ((GameSetupActivity) getActivity()).computeStartLayoutVisibility();
        }
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
    }
}