package com.tonkar.volleyballreferee.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.Tags;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class CodeInputDialogFragment extends DialogFragment {

    private AlertDialogListener mAlertDialogListener;

    public static CodeInputDialogFragment newInstance(String title, String negativeButtonText, String positiveButtonText) {
        CodeInputDialogFragment fragment = new CodeInputDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("negative", negativeButtonText);
        args.putString("positive", positiveButtonText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(title);
        builder.setView(getActivity().getLayoutInflater().inflate(R.layout.code_input_dialog, null));

        String negativeButtonText = getArguments().getString("negative");
        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, (dialog, which) -> mAlertDialogListener.onNegativeButtonClicked());
        }

        String positiveButtonText = getArguments().getString("positive");
        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                EditText editText = ((AlertDialog) dialog).findViewById(R.id.code_input);
                String codeStr = editText.getText().toString();

                int code = -1;

                try {
                    code = Integer.valueOf(codeStr);
                } catch (NumberFormatException e) {
                    Log.e(Tags.CODE, String.format("Error while reading the code %s", codeStr), e);
                }

                mAlertDialogListener.onPositiveButtonClicked(code);
            });
        }

        setCancelable(false);

        return builder.create();
    }

    public void setAlertDialogListener(AlertDialogListener alertDialogListener) {
        mAlertDialogListener = alertDialogListener;
    }

    public interface AlertDialogListener {

        void onNegativeButtonClicked();

        void onPositiveButtonClicked(int code);

    }

}
