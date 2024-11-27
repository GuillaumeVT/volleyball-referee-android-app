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

import org.apache.commons.lang3.StringUtils;

import java.net.HttpURLConnection;
import java.util.Locale;

public class UserAccountFragment extends Fragment {

    public UserAccountFragment() {}

    public static UserAccountFragment newInstance() {
        return new UserAccountFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.USER_UI, "Create user account fragment");
        View fragmentView = inflater.inflate(R.layout.fragment_user_account, container, false);

        UserSummaryDto user = PrefUtils.getUser(getContext());
        if (user != null) {
            EditText pseudoText = fragmentView.findViewById(R.id.user_pseudo_input_text);
            pseudoText.setEnabled(false);
            pseudoText.setText(user.getPseudo());

            Button signOutButton = fragmentView.findViewById(R.id.user_sign_out_button);
            signOutButton.setOnClickListener(button -> {
                PrefUtils.signOut(getContext());
                UiUtils.makeText(getContext(), getString(R.string.user_signed_out), Toast.LENGTH_LONG).show();
                UiUtils.navigateToMain(requireActivity(), R.id.user_fragment);
            });

            Button updatePasswordButton = fragmentView.findViewById(R.id.user_password_update_button);
            updatePasswordButton.setOnClickListener(button -> onUpdatePasswordClicked(fragmentView));
        }

        return fragmentView;
    }

    private void onUpdatePasswordClicked(View fragmentView) {
        TextInputLayout currentPasswordInputLayout = fragmentView.findViewById(R.id.user_current_password_input_layout);
        TextInputLayout newPasswordInputLayout = fragmentView.findViewById(R.id.user_new_password_input_layout);
        TextInputLayout confirmNewPasswordInputLayout = fragmentView.findViewById(R.id.user_confirm_new_password_input_layout);

        EditText currentPasswordInputText = fragmentView.findViewById(R.id.user_current_password_input_text);
        EditText newPasswordInputText = fragmentView.findViewById(R.id.user_new_password_input_text);
        EditText confirmNewPasswordInputText = fragmentView.findViewById(R.id.user_confirm_new_password_input_text);

        final String currentPassword = StringUtils.trimToEmpty(currentPasswordInputText.getText().toString());
        final String newPassword = StringUtils.trimToEmpty(newPasswordInputText.getText().toString());
        final String confirmedNewPassword = StringUtils.trimToEmpty(confirmNewPasswordInputText.getText().toString());

        currentPasswordInputLayout.setErrorEnabled(currentPassword.isEmpty());
        newPasswordInputLayout.setErrorEnabled(newPassword.isEmpty());
        confirmNewPasswordInputLayout.setErrorEnabled(confirmedNewPassword.isEmpty());

        if (!newPassword.equals(confirmedNewPassword)) {
            newPasswordInputLayout.setError(getString(R.string.user_password_not_matching));
            confirmNewPasswordInputLayout.setError(getString(R.string.user_password_not_matching));
        }

        if (!currentPassword.isEmpty() && !newPassword.isEmpty() && !confirmedNewPassword.isEmpty() && newPassword.equals(
                confirmedNewPassword)) {
            currentPasswordInputLayout.setError(null);
            newPasswordInputLayout.setError(null);
            confirmNewPasswordInputLayout.setError(null);

            StoredUserService storedUserService = new StoredUserManager(getContext());
            storedUserService.updateUserPassword(new UserPasswordUpdateDto(currentPassword, newPassword), new AsyncUserRequestListener() {
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
                                        currentPasswordInputLayout.setError(getString(R.string.user_password_error));
                                case HttpURLConnection.HTTP_BAD_REQUEST -> {
                                    newPasswordInputLayout.setError(getString(R.string.password_strength_error));
                                    confirmNewPasswordInputLayout.setError(getString(R.string.password_strength_error));
                                }
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
