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
import com.tonkar.volleyballreferee.engine.stored.AsyncUserRequestListener;
import com.tonkar.volleyballreferee.engine.stored.StoredUserManager;
import com.tonkar.volleyballreferee.engine.stored.StoredUserService;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserPasswordUpdate;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserToken;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.net.HttpURLConnection;
import java.util.Locale;

public class UserAccountFragment extends Fragment {

    private View mView;

    public UserAccountFragment() {}

    public static UserAccountFragment newInstance() {
        return new UserAccountFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.USER_UI, "Create user account fragment");
        mView = inflater.inflate(R.layout.fragment_user_account, container, false);

        ApiUserSummary user = PrefUtils.getUser(getContext());
        if (!ApiUserSummary.emptyUser().equals(user)) {
            EditText emailText = mView.findViewById(R.id.user_email_input_text);
            emailText.setEnabled(false);
            emailText.setText(user.getEmail());

            EditText pseudoText = mView.findViewById(R.id.user_pseudo_input_text);
            pseudoText.setEnabled(false);
            pseudoText.setText(user.getPseudo());

            Button signOutButton = mView.findViewById(R.id.user_sign_out_button);
            signOutButton.setOnClickListener(button -> {
                PrefUtils.signOut(getContext());
                UiUtils.makeText(getContext(), getString(R.string.user_signed_out), Toast.LENGTH_LONG).show();
                UiUtils.navigateToHome(getActivity());
            });

            Button updatePasswordButton = mView.findViewById(R.id.user_password_update_button);
            updatePasswordButton.setOnClickListener(button -> onUpdatePasswordClicked());
        }

        return mView;
    }

    private void onUpdatePasswordClicked() {
        TextInputLayout currentPasswordInputLayout = mView.findViewById(R.id.user_current_password_input_layout);
        TextInputLayout newPasswordInputLayout = mView.findViewById(R.id.user_new_password_input_layout);
        TextInputLayout confirmNewPasswordInputLayout = mView.findViewById(R.id.user_confirm_new_password_input_layout);

        EditText currentPasswordInputText = mView.findViewById(R.id.user_current_password_input_text);
        EditText newPasswordInputText = mView.findViewById(R.id.user_new_password_input_text);
        EditText confirmNewPasswordInputText = mView.findViewById(R.id.user_confirm_new_password_input_text);

        final String currentPassword = currentPasswordInputText.getText().toString().trim();
        final String newPassword = newPasswordInputText.getText().toString().trim();
        final String confirmedNewPassword = confirmNewPasswordInputText.getText().toString().trim();

        currentPasswordInputLayout.setErrorEnabled(currentPassword.isEmpty());
        newPasswordInputLayout.setErrorEnabled(newPassword.isEmpty());
        confirmNewPasswordInputLayout.setErrorEnabled(confirmedNewPassword.isEmpty());

        if (!newPassword.equals(confirmedNewPassword)) {
            newPasswordInputLayout.setError(getString(R.string.user_password_not_matching));
            confirmNewPasswordInputLayout.setError(getString(R.string.user_password_not_matching));
        }

        if (!currentPassword.isEmpty() && !newPassword.isEmpty() && !confirmedNewPassword.isEmpty() && newPassword.equals(confirmedNewPassword)) {
            currentPasswordInputLayout.setError(null);
            newPasswordInputLayout.setError(null);
            confirmNewPasswordInputLayout.setError(null);

            StoredUserService storedUserService = new StoredUserManager(getContext());
            storedUserService.updateUserPassword(new ApiUserPasswordUpdate(currentPassword, newPassword), new AsyncUserRequestListener() {
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
                                currentPasswordInputLayout.setError(getString(R.string.user_password_error));
                                break;
                            case HttpURLConnection.HTTP_BAD_REQUEST:
                                newPasswordInputLayout.setError(getString(R.string.password_strength_error));
                                confirmNewPasswordInputLayout.setError(getString(R.string.password_strength_error));
                                break;
                            default:
                                UiUtils.makeText(getContext(), getString(R.string.user_request_error), Toast.LENGTH_LONG).show();
                                break;
                        }
                    });
                }
            });
        }
    }
}
