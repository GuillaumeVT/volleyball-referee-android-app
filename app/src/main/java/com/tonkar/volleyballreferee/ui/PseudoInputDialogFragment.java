package com.tonkar.volleyballreferee.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.tonkar.volleyballreferee.api.ApiUser;
import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.StoredUser;
import com.tonkar.volleyballreferee.business.data.SyncWorker;
import com.tonkar.volleyballreferee.interfaces.data.AsyncUserRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredUserService;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.net.HttpURLConnection;
import java.util.Locale;

public class PseudoInputDialogFragment extends DialogFragment {

    public static PseudoInputDialogFragment newInstance() {
        PseudoInputDialogFragment fragment = new PseudoInputDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.create_user_pseudo_title))
                .setView(getActivity().getLayoutInflater().inflate(R.layout.pseudo_input_dialog, null))
                .setPositiveButton(getString(android.R.string.ok), null)
                .setCancelable(false);

        setCancelable(false);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                Authentication authentication = PrefUtils.getAuthentication(getContext());
                EditText editText = dialog.findViewById(R.id.pseudo_input_text);
                String pseudo = editText.getText().toString().trim();
                TextInputLayout editTextLayout = dialog.findViewById(R.id.pseudo_input_layout);
                StoredUserService storedUserService = new StoredUser(getContext());
                if (pseudo.length() > 2) {
                    storedUserService.createUser(authentication.getUserId(), pseudo, new AsyncUserRequestListener() {
                        @Override
                        public void onUserCreated(ApiUser user) {
                            SyncWorker.syncAll(getActivity().getApplicationContext());
                            dialog.dismiss();
                            UiUtils.makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.user_signed_in_as_pseudo), user.getPseudo()), Toast.LENGTH_LONG).show();
                            UiUtils.navigateToHome(getActivity());
                        }

                        @Override
                        public void onUserReceived(ApiUser user) {}

                        @Override
                        public void onError(int httpCode) {
                            if (HttpURLConnection.HTTP_CONFLICT == httpCode) {
                                editTextLayout.setError(getString(R.string.user_exists_error));
                            } else {
                                editTextLayout.setError(getString(R.string.user_creation_error));
                            }
                        }
                    });
                } else {
                    editTextLayout.setError(String.format(getString(R.string.minimum_size_error), 3));
                }
            });
        });

        return dialog;
    }

}
