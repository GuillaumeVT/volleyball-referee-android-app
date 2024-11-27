package com.tonkar.volleyballreferee.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.UserTokenDto;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.net.HttpURLConnection;
import java.util.Locale;

public class UserSignInFragment extends Fragment {

    public UserSignInFragment() {}

    public static UserSignInFragment newInstance() {
        return new UserSignInFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.USER_UI, "Create user sign in fragment");
        View fragmentView = inflater.inflate(R.layout.fragment_user_sign_in, container, false);

        Button signInButton = fragmentView.findViewById(R.id.user_sign_in_button);
        signInButton.setOnClickListener(button -> onSignInClicked(fragmentView));

        return fragmentView;
    }

    private void onSignInClicked(View fragmentView) {
        TextInputLayout pseudoInputLayout = fragmentView.findViewById(R.id.user_pseudo_input_layout);
        EditText pseudoInputText = fragmentView.findViewById(R.id.user_pseudo_input_text);

        TextInputLayout passwordInputLayout = fragmentView.findViewById(R.id.user_password_input_layout);
        EditText passwordInputText = fragmentView.findViewById(R.id.user_password_input_text);

        final String pseudo = StringUtils.trimToEmpty(pseudoInputText.getText().toString());
        final String password = StringUtils.trimToEmpty(passwordInputText.getText().toString());

        pseudoInputLayout.setError(null);
        passwordInputLayout.setError(null);

        if (StringUtils.isBlank(pseudo)) {
            pseudoInputLayout.setErrorEnabled(true);
        } else if (StringUtils.isBlank(password)) {
            passwordInputLayout.setErrorEnabled(true);
        } else {
            pseudoInputLayout.setError(null);
            passwordInputLayout.setError(null);

            StoredUserService storedUserService = new StoredUserManager(getContext());
            storedUserService.signInUser(pseudo, password, new AsyncUserRequestListener() {
                @Override
                public void onUserTokenReceived(UserTokenDto userToken) {
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
}
