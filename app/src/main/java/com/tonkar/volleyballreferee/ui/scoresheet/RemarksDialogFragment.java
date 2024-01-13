package com.tonkar.volleyballreferee.ui.scoresheet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.tonkar.volleyballreferee.R;

public class RemarksDialogFragment extends DialogFragment {

    private View               mView;
    private EditText           mObservationInputText;
    private ScoreSheetActivity mScoreSheetActivity;

    public static RemarksDialogFragment newInstance() {
        return new RemarksDialogFragment();
    }

    public RemarksDialogFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mView;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        mScoreSheetActivity = (ScoreSheetActivity) requireActivity();

        mView = mScoreSheetActivity.getLayoutInflater().inflate(R.layout.remarks_dialog, null);

        mObservationInputText = mView.findViewById(R.id.observation_input_text);
        mObservationInputText.setText(mScoreSheetActivity.getScoreSheetBuilder().getRemarks());

        return new AlertDialog.Builder(requireContext(), R.style.AppTheme_Dialog)
                .setView(mView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> setObservations())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                .create();
    }

    private void setObservations() {
        mScoreSheetActivity.getScoreSheetBuilder().setRemarks(mObservationInputText.getText().toString());
        mScoreSheetActivity.loadScoreSheet(false);
    }

}
