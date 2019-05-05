package com.tonkar.volleyballreferee.ui.setup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiLeagueDescription;
import com.tonkar.volleyballreferee.business.data.StoredLeagues;
import com.tonkar.volleyballreferee.interfaces.BaseGeneralService;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.StoredLeaguesService;
import com.tonkar.volleyballreferee.ui.interfaces.BaseGeneralServiceHandler;
import com.tonkar.volleyballreferee.ui.util.ClearableTextInputAutoCompleteTextView;

import java.util.ArrayList;
import java.util.UUID;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SETUP_UI, "Create misc setup fragment");
        View view = inflater.inflate(R.layout.fragment_misc_setup, container, false);

        StoredLeaguesService storedLeaguesService = new StoredLeagues(getContext());

        final ClearableTextInputAutoCompleteTextView divisionNameInput = view.findViewById(R.id.division_name_input_text);
        divisionNameInput.setThreshold(2);
        divisionNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update division name");
                mGeneralService.setDivisionName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        divisionNameInput.setText(mGeneralService.getDivisionName());

        final ClearableTextInputAutoCompleteTextView leagueNameInput = view.findViewById(R.id.league_name_input_text);
        leagueNameInput.setThreshold(2);
        leagueNameInput.setAdapter(new AutocompleteLeagueListAdapter(getContext(), getLayoutInflater(), storedLeaguesService.listLeagues(mGeneralService.getKind())));
        leagueNameInput.setOnItemClickListener((parent, input, index, id) -> {
            ApiLeagueDescription leagueDescription = (ApiLeagueDescription) leagueNameInput.getAdapter().getItem(index);
            leagueNameInput.setText(leagueDescription.getName());
            mGeneralService.setLeagueId(leagueDescription.getId());
            mGeneralService.setLeagueName(leagueDescription.getName());
            divisionNameInput.setText("");
            divisionNameInput.setAdapter(new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, new ArrayList<>(storedLeaguesService.listDivisionNames(leagueDescription.getId()))));
            mGeneralService.setDivisionName("");
        });

        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update league name");
                // TODO check if triggerred when autocomplete
                mGeneralService.setLeagueId(UUID.randomUUID().toString());
                mGeneralService.setLeagueName(s.toString());
                divisionNameInput.setAdapter(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        leagueNameInput.setText(mGeneralService.getLeagueName());

        // TODO referee spinner

        return view;
    }

    @Override
    public void setGeneralService(BaseGeneralService generalService) {
        mGeneralService = generalService;
    }
}