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
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.ui.util.ClearableTextInputAutoCompleteTextView;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;

public class MiscSetupFragment extends Fragment {

    public MiscSetupFragment() {
    }

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

        if (ServicesProvider.getInstance().areSetupServicesUnavailable()) {
            ServicesProvider.getInstance().restoreGameServiceForSetup(getActivity().getApplicationContext());
        }

        final ClearableTextInputAutoCompleteTextView leagueNameInput = view.findViewById(R.id.league_name_input_text);
        leagueNameInput.setThreshold(2);
        ArrayAdapter<String> leagueNameAdapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, new ArrayList<>(ServicesProvider.getInstance().getRecordedGamesService().getRecordedLeagues()));
        leagueNameInput.setAdapter(leagueNameAdapter);
        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update league name");
                ServicesProvider.getInstance().getGeneralService().setLeagueName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        leagueNameInput.setText(ServicesProvider.getInstance().getGeneralService().getLeagueName());

        final ClearableTextInputAutoCompleteTextView divisionNameInput = view.findViewById(R.id.division_name_input_text);
        divisionNameInput.setThreshold(2);
        ArrayAdapter<String> divisionNameAdapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, new ArrayList<>(ServicesProvider.getInstance().getRecordedGamesService().getRecordedDivisions()));
        divisionNameInput.setAdapter(divisionNameAdapter);
        divisionNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update division name");
                ServicesProvider.getInstance().getGeneralService().setDivisionName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        divisionNameInput.setText(ServicesProvider.getInstance().getGeneralService().getDivisionName());

        return view;
    }

}