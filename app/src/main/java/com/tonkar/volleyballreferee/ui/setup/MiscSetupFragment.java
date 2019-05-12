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
import android.widget.Spinner;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiFriend;
import com.tonkar.volleyballreferee.api.ApiLeagueDescription;
import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.StoredLeagues;
import com.tonkar.volleyballreferee.business.data.StoredUser;
import com.tonkar.volleyballreferee.interfaces.BaseGeneralService;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.StoredLeaguesService;
import com.tonkar.volleyballreferee.interfaces.data.StoredUserService;
import com.tonkar.volleyballreferee.ui.interfaces.BaseGeneralServiceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.fragment.app.Fragment;

public class MiscSetupFragment extends Fragment implements BaseGeneralServiceHandler {

    private BaseGeneralService mGeneralService;

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

        StoredLeaguesService storedLeaguesService = new StoredLeagues(getContext());
        StoredUserService storedUserService = new StoredUser(getContext());

        final AutoCompleteTextView divisionNameInput = view.findViewById(R.id.division_name_input_text);
        divisionNameInput.setThreshold(2);
        divisionNameInput.setOnItemClickListener((parent, input, index, id) -> {
            mGeneralService.getLeague().setDivision((String) divisionNameInput.getAdapter().getItem(index));
            computeConfirmItemVisibility();
        });
        divisionNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update division");
                mGeneralService.getLeague().setDivision(s.toString().trim());
                ((TextInputLayout)view.findViewById(R.id.division_name_input_layout)).setError(count < 2 ? String.format(Locale.getDefault(), getString(R.string.must_provide_at_least_n_characters), 2) : null);
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        divisionNameInput.setText(mGeneralService.getLeague().getDivision());

        final AutoCompleteTextView leagueNameInput = view.findViewById(R.id.league_name_input_text);
        leagueNameInput.setThreshold(2);
        leagueNameInput.setAdapter(new AutocompleteLeagueListAdapter(getContext(), getLayoutInflater(), storedLeaguesService.listLeagues(mGeneralService.getKind())));
        leagueNameInput.setOnItemClickListener((parent, input, index, id) -> {
            ApiLeagueDescription leagueDescription = (ApiLeagueDescription) leagueNameInput.getAdapter().getItem(index);
            leagueNameInput.setText(leagueDescription.getName());
            mGeneralService.getLeague().setAll(leagueDescription);
            divisionNameInput.setText("");
            divisionNameInput.setAdapter(new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, new ArrayList<>(storedLeaguesService.listDivisionNames(leagueDescription.getId()))));
            computeConfirmItemVisibility();
        });

        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update league");
                mGeneralService.getLeague().setName(s.toString().trim());
                view.findViewById(R.id.division_name_input_layout).setVisibility(count == 0 ? View.GONE : View.VISIBLE);
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        leagueNameInput.setText(mGeneralService.getLeague().getName());

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
                refereeSpinner.setSelection(refereeAdapter.getPositionFromId(mGeneralService.getRefereedBy()));
            }
            refereeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(Tags.SETUP_UI, "Update referee");
                    ApiFriend referee = refereeAdapter.getItem(position);
                    mGeneralService.setRefereedBy(referee.getId());
                    mGeneralService.setRefereeName(referee.getPseudo());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mGeneralService.setRefereedBy(Authentication.VBR_USER_ID);
                    mGeneralService.setRefereeName("");
                }
            });
        } else {
            view.findViewById(R.id.referee_spinner_title).setVisibility(View.GONE);
            refereeSpinner.setVisibility(View.GONE);
        }

        return view;
    }

    private void computeConfirmItemVisibility() {
        if (getActivity() instanceof GameSetupActivity) {
            ((GameSetupActivity) getActivity()).computeStartItemVisibility();
        }
    }

    @Override
    public void setGeneralService(BaseGeneralService generalService) {
        mGeneralService = generalService;
    }
}