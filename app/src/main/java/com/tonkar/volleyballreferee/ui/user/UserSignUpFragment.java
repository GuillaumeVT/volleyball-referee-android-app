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
import com.tonkar.volleyballreferee.engine.api.model.ApiNewUser;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserToken;
import com.tonkar.volleyballreferee.engine.service.AsyncUserRequestListener;
import com.tonkar.volleyballreferee.engine.service.StoredUserManager;
import com.tonkar.volleyballreferee.engine.service.StoredUserService;
import com.tonkar.volleyballreferee.ui.util.ProgressIndicatorActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.net.HttpURLConnection;
import java.util.Locale;
import java.util.UUID;

public class UserSignUpFragment extends Fragment {

    private View       mView;
    private ApiNewUser mNewUser;

    public UserSignUpFragment() {}

    public static UserSignUpFragment newInstance() {
        return new UserSignUpFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.USER_UI, "Create user sign up fragment");
        mView = inflater.inflate(R.layout.fragment_user_sign_up, container, false);

        mNewUser = new ApiNewUser();
        mNewUser.setPurchaseToken(PrefUtils.getWebPremiumBillingToken(getContext()));
        mNewUser.setId(UUID.randomUUID().toString());
        mNewUser.setEmail("");
        mNewUser.setPseudo("");
        mNewUser.setPassword("");

        Button signUpButton = mView.findViewById(R.id.user_sign_up_button);
        signUpButton.setOnClickListener(button -> onSignUpClicked());

        return mView;
    }

    private void onSignUpClicked() {
        TextInputLayout emailInputLayout = mView.findViewById(R.id.user_email_input_layout);
        TextInputLayout confirmEmailInputLayout = mView.findViewById(R.id.user_confirm_email_input_layout);
        TextInputLayout pseudoInputLayout = mView.findViewById(R.id.user_pseudo_input_layout);
        TextInputLayout passwordInputLayout = mView.findViewById(R.id.user_new_password_input_layout);
        TextInputLayout confirmPasswordInputLayout = mView.findViewById(R.id.user_confirm_new_password_input_layout);

        EditText emailInputText = mView.findViewById(R.id.user_email_input_text);
        EditText confirmEmailInputText = mView.findViewById(R.id.user_confirm_email_input_text);
        EditText pseudoInputText = mView.findViewById(R.id.user_pseudo_input_text);
        EditText passwordInputText = mView.findViewById(R.id.user_new_password_input_text);
        EditText confirmPasswordInputText = mView.findViewById(R.id.user_confirm_new_password_input_text);

        final String email = emailInputText.getText().toString().trim();
        final String confirmedEmail = confirmEmailInputText.getText().toString().trim();
        final String pseudo = pseudoInputText.getText().toString().trim();
        final String password = passwordInputText.getText().toString().trim();
        final String confirmedPassword = confirmPasswordInputText.getText().toString().trim();

        emailInputLayout.setErrorEnabled(email.isEmpty());
        confirmEmailInputLayout.setErrorEnabled(confirmedEmail.isEmpty());

        pseudoInputLayout.setErrorEnabled(pseudo.isEmpty());

        passwordInputLayout.setErrorEnabled(password.isEmpty());
        confirmPasswordInputLayout.setErrorEnabled(confirmedPassword.isEmpty());

        if (!email.equals(confirmedEmail)) {
            emailInputLayout.setError(getString(R.string.user_email_not_matching));
            confirmEmailInputLayout.setError(getString(R.string.user_email_not_matching));
        }

        if (!password.equals(confirmedPassword)) {
            passwordInputLayout.setError(getString(R.string.user_password_not_matching));
            confirmPasswordInputLayout.setError(getString(R.string.user_password_not_matching));
        }

        if (!email.isEmpty() && !confirmedEmail.isEmpty() && email.equals(confirmedEmail)
                && !password.isEmpty() && !confirmedPassword.isEmpty() && password.equals(confirmedPassword)
                && !pseudo.isEmpty()) {
            emailInputLayout.setError(null);
            confirmEmailInputLayout.setError(null);
            pseudoInputLayout.setError(null);
            passwordInputLayout.setError(null);
            confirmPasswordInputLayout.setError(null);

            mNewUser.setEmail(email);
            mNewUser.setPseudo(pseudo);
            mNewUser.setPassword(password);

            showProgressIndicator();
            StoredUserService storedUserService = new StoredUserManager(getContext());
            storedUserService.createUser(mNewUser, new AsyncUserRequestListener() {
                @Override
                public void onUserReceived(ApiUserSummary user) {}

                @Override
                public void onUserTokenReceived(ApiUserToken userToken) {
                    getActivity().runOnUiThread(() -> {
                        hideProgressIndicator();
                        UiUtils.makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.user_signed_in_as_pseudo), userToken.getUser().getPseudo()), Toast.LENGTH_LONG).show();
                        UiUtils.navigateToHome(getActivity());
                    });
                }

                @Override
                public void onUserPasswordRecoveryInitiated() {}

                @Override
                public void onError(int httpCode) {
                    getActivity().runOnUiThread(() -> {
                        hideProgressIndicator();
                        switch (httpCode) {
                            case HttpURLConnection.HTTP_CONFLICT:
                                emailInputLayout.setError(getString(R.string.user_email_pseudo_conflict));
                                confirmEmailInputLayout.setError(getString(R.string.user_email_pseudo_conflict));
                                pseudoInputLayout.setError(getString(R.string.user_email_pseudo_conflict));
                                break;
                            case HttpURLConnection.HTTP_BAD_REQUEST:
                                passwordInputLayout.setError(getString(R.string.password_strength_error));
                                confirmPasswordInputLayout.setError(getString(R.string.password_strength_error));
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

    private void showProgressIndicator() {
        if (isAdded()) {
            ((ProgressIndicatorActivity) getActivity()).showProgressIndicator();
        }
    }

    private void hideProgressIndicator() {
        if (isAdded()) {
            ((ProgressIndicatorActivity) getActivity()).hideProgressIndicator();
        }
    }

}
