package com.tonkar.volleyballreferee.ui.util;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.tonkar.volleyballreferee.R;

public class AlertDialogFragment extends DialogFragment {

    private AlertDialogListener mAlertDialogListener;

    public static AlertDialogFragment newInstance(String title, String message, String negativeButtonText) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("content", message);
        args.putString("negative", negativeButtonText);
        fragment.setArguments(args);
        return fragment;
    }

    public static AlertDialogFragment newInstance(String title, String message, String negativeButtonText, String positiveButtonText) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("content", message);
        args.putString("negative", negativeButtonText);
        args.putString("positive", positiveButtonText);
        fragment.setArguments(args);
        return fragment;
    }

    public static AlertDialogFragment newInstance(String title,
                                                  String message,
                                                  String negativeButtonText,
                                                  String positiveButtonText,
                                                  String neutralButtonText) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("content", message);
        args.putString("negative", negativeButtonText);
        args.putString("positive", positiveButtonText);
        args.putString("neutral", neutralButtonText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = requireArguments();
        String title = bundle.getString("title");
        String message = bundle.getString("content");

        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(title).setMessage(message);

        String negativeButtonText = bundle.getString("negative");
        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, (dialog, which) -> mAlertDialogListener.onNegativeButtonClicked());
        }

        String positiveButtonText = bundle.getString("positive");
        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> mAlertDialogListener.onPositiveButtonClicked());
        }

        String neutralButtonText = bundle.getString("neutral");
        if (neutralButtonText != null) {
            builder.setNeutralButton(neutralButtonText, (dialog, which) -> mAlertDialogListener.onNeutralButtonClicked());
        }

        setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            if (isAdded()) {
                UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
            }
        });
        return alertDialog;
    }

    public void setAlertDialogListener(AlertDialogListener alertDialogListener) {
        mAlertDialogListener = alertDialogListener;
    }

    public interface AlertDialogListener {

        void onNegativeButtonClicked();

        void onPositiveButtonClicked();

        void onNeutralButtonClicked();

    }

}
