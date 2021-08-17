package com.tonkar.volleyballreferee.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserToken;
import com.tonkar.volleyballreferee.engine.service.AsyncUserRequestListener;
import com.tonkar.volleyballreferee.engine.service.StoredUserManager;
import com.tonkar.volleyballreferee.engine.service.StoredUserService;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.net.HttpURLConnection;
import java.util.Locale;

public class UserSignInFragment extends Fragment {

    private View           mView;
    private ApiUserSummary mUser;

    public UserSignInFragment() {}

    public static UserSignInFragment newInstance() {
        return new UserSignInFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.USER_UI, "Create user sign in fragment");
        mView = inflater.inflate(R.layout.fragment_user_sign_in, container, false);

        mUser = PrefUtils.getUser(getContext());
        if (!ApiUserSummary.emptyUser().equals(mUser)) {
            EditText emailText = mView.findViewById(R.id.user_email_input_text);
            emailText.setEnabled(false);
            emailText.setText(mUser.getEmail());

            EditText pseudoText = mView.findViewById(R.id.user_pseudo_input_text);
            pseudoText.setEnabled(false);
            pseudoText.setText(mUser.getPseudo());

            Button signInButton = mView.findViewById(R.id.user_sign_in_button);
            signInButton.setOnClickListener(button -> onSignInClicked());

            Button lostPasswordButton = mView.findViewById(R.id.lost_password_button);
            lostPasswordButton.setOnClickListener(button -> onLostPasswordClicked());
        }

        return mView;
    }

    private void onSignInClicked() {
        TextInputLayout passwordInputLayout = mView.findViewById(R.id.user_new_password_input_layout);
        EditText passwordInputText = mView.findViewById(R.id.user_new_password_input_text);
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
                    getActivity().runOnUiThread(() -> {
                        UiUtils.makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.user_signed_in_as_pseudo), userToken.getUser().getPseudo()), Toast.LENGTH_LONG).show();
                        UiUtils.navigateToHome(getActivity());
                    });
                }

                @Override
                public void onUserPasswordRecoveryInitiated() {}

                @Override
                public void onError(int httpCode) {
                    getActivity().runOnUiThread(() -> {
                        switch (httpCode) {
                            case HttpURLConnection.HTTP_UNAUTHORIZED:
                                passwordInputLayout.setError(getString(R.string.user_password_error));
                                break;
                            case HttpURLConnection.HTTP_FORBIDDEN:
                                passwordInputLayout.setError(getString(R.string.user_locked_error));
                                break;
                            default:
                                UiUtils.makeErrorText(getContext(), getString(R.string.user_request_error), Toast.LENGTH_LONG).show();
                                break;
                        }
                    });
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
                getActivity().runOnUiThread(() -> UiUtils.makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.user_password_recovery_initiated), mUser.getEmail()), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onError(int httpCode) {
                getActivity().runOnUiThread(() -> UiUtils.makeErrorText(getContext(), getString(R.string.user_request_error), Toast.LENGTH_LONG).show());
            }
        });

    }

}
