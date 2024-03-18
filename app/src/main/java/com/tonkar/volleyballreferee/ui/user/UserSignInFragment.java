package com.tonkar.volleyballreferee.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.net.HttpURLConnection;
import java.util.Locale;

public class UserSignInFragment extends Fragment {

    private ApiUserSummary mUser;

    public UserSignInFragment() {}

    public static UserSignInFragment newInstance() {
        return new UserSignInFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.USER_UI, "Create user sign in fragment");
        View fragmentView = inflater.inflate(R.layout.fragment_user_sign_in, container, false);

        mUser = PrefUtils.getUser(getContext());
        if (!ApiUserSummary.emptyUser().equals(mUser)) {
            EditText emailText = fragmentView.findViewById(R.id.user_email_input_text);
            emailText.setEnabled(false);
            emailText.setText(mUser.getEmail());

            EditText pseudoText = fragmentView.findViewById(R.id.user_pseudo_input_text);
            pseudoText.setEnabled(false);
            pseudoText.setText(mUser.getPseudo());

            Button signInButton = fragmentView.findViewById(R.id.user_sign_in_button);
            signInButton.setOnClickListener(button -> onSignInClicked(fragmentView));

            Button lostPasswordButton = fragmentView.findViewById(R.id.lost_password_button);
            lostPasswordButton.setOnClickListener(button -> onLostPasswordClicked());
        }

        return fragmentView;
    }

    private void onSignInClicked(View fragmentView) {
        TextInputLayout passwordInputLayout = fragmentView.findViewById(R.id.user_new_password_input_layout);
        EditText passwordInputText = fragmentView.findViewById(R.id.user_new_password_input_text);
        final String password = passwordInputText.getText().toString().trim();

        passwordInputLayout.setError(null);

        if (password.isEmpty()) {
            passwordInputLayout.setErrorEnabled(true);
        } else {
            passwordInputLayout.setError(null);
            StoredUserService storedUserService = new StoredUserManager(getContext());
            storedUserService.signInUser(mUser.getEmail(), password, new AsyncUserRequestListener() {
                @Override
                public void onUserReceived(ApiUserSummary user) {}

                @Override
                public void onUserTokenReceived(ApiUserToken userToken) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            UiUtils
                                    .makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.user_signed_in_as_pseudo),
                                                                          userToken.getUser().getPseudo()), Toast.LENGTH_LONG)
                                    .show();
                            UiUtils.navigateToMain(requireActivity(), R.id.user_fragment);
                        });
                    }
                }

                @Override
                public void onUserPasswordRecoveryInitiated() {}

                @Override
                public void onError(int httpCode) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            switch (httpCode) {
                                case HttpURLConnection.HTTP_UNAUTHORIZED ->
                                        passwordInputLayout.setError(getString(R.string.user_password_error));
                                case HttpURLConnection.HTTP_FORBIDDEN ->
                                        passwordInputLayout.setError(getString(R.string.user_locked_error));
                                default -> UiUtils
                                        .makeErrorText(getContext(), getString(R.string.user_request_error), Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                }
            });
        }
    }

    private void onLostPasswordClicked() {
        StoredUserService storedUserService = new StoredUserManager(getContext());
        storedUserService.initiateUserPasswordRecovery(mUser.getEmail(), new AsyncUserRequestListener() {
            @Override
            public void onUserReceived(ApiUserSummary user) {}

            @Override
            public void onUserTokenReceived(ApiUserToken userToken) {}

            @Override
            public void onUserPasswordRecoveryInitiated() {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> UiUtils
                            .makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.user_password_recovery_initiated),
                                                                  mUser.getEmail()), Toast.LENGTH_LONG)
                            .show());
                }
            }

            @Override
            public void onError(int httpCode) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(
                            () -> UiUtils.makeErrorText(getContext(), getString(R.string.user_request_error), Toast.LENGTH_LONG).show());
                }
            }
        });

    }

}
