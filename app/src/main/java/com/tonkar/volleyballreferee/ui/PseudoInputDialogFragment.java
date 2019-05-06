package com.tonkar.volleyballreferee.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import com.tonkar.volleyballreferee.R;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.tonkar.volleyballreferee.api.ApiFriendRequest;
import com.tonkar.volleyballreferee.api.ApiUser;
import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.StoredUser;
import com.tonkar.volleyballreferee.interfaces.data.AsyncUserRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredUserService;

import java.net.HttpURLConnection;
import java.util.List;

public class PseudoInputDialogFragment extends DialogFragment {

    public static PseudoInputDialogFragment newInstance() {
        PseudoInputDialogFragment fragment = new PseudoInputDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.user_pseudo_title));
        builder.setView(getActivity().getLayoutInflater().inflate(R.layout.pseudo_input_dialog, null));
        builder.setPositiveButton(getString(android.R.string.ok), null);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                Authentication authentication = PrefUtils.getAuthentication(getContext());
                EditText editText = dialog.findViewById(R.id.pseudo_input);
                String pseudo = editText.getText().toString().trim();
                StoredUserService storedUserService = new StoredUser(getContext());
                if (pseudo.length() >= 3) {
                    storedUserService.createUser(authentication.getUserId(), pseudo, new AsyncUserRequestListener() {
                        @Override
                        public void onUserCreated() {
                            dialog.dismiss();
                        }

                        @Override
                        public void onUserReceived(ApiUser user) {}

                        @Override
                        public void onFriendRequestsReceived(List<ApiFriendRequest> friendRequests) {}

                        @Override
                        public void onError(int httpCode) {
                            if (HttpURLConnection.HTTP_CONFLICT == httpCode) {
                                editText.setError(getString(R.string.user_exists_error));
                            } else {
                                editText.setError(getString(R.string.user_creation_error));
                            }
                        }
                    });
                } else {
                    editText.setError(String.format(getString(R.string.minimum_size_error), 3));
                }
            });
        });

        return dialog;
    }

}
