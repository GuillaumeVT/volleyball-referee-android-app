package com.tonkar.volleyballreferee.ui.stored.game.scoresheet;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ui.setup.NameSpinnerAdapter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class SignatureDialogFragment extends DialogFragment {

    private Context            mContext;
    private LayoutInflater     mLayoutInflater;
    private View               mView;
    private EditText           mNameInputText;
    private SignatureView      mSignatureView;
    private int                mSelectedSignatureType;
    private ScoreSheetActivity mScoreSheetActivity;

    public static SignatureDialogFragment newInstance(String homeTeamName, String guestTeamName, String homeCaptainName, String guestCaptainName) {
        SignatureDialogFragment fragment = new SignatureDialogFragment();
        Bundle args = new Bundle();
        args.putString("homeTeamName", homeTeamName);
        args.putString("guestTeamName", guestTeamName);
        args.putString("homeCaptainName", homeCaptainName);
        args.putString("guestCaptainName", guestCaptainName);
        fragment.setArguments(args);
        return fragment;
    }

    public SignatureDialogFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mView;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        String homeTeamName = getArguments().getString("homeTeamName");
        String guestTeamName = getArguments().getString("guestTeamName");
        String homeCaptainName = getArguments().getString("homeCaptainName");
        String guestCaptainName = getArguments().getString("guestCaptainName");

        mScoreSheetActivity = (ScoreSheetActivity) getActivity();

        mLayoutInflater = mScoreSheetActivity.getLayoutInflater();
        mContext = getContext();
        mView = mLayoutInflater.inflate(R.layout.signature_dialog, null);

        mNameInputText = mView.findViewById(R.id.name_input_text);

        mSignatureView = mView.findViewById(R.id.signature_canvas);

        mSelectedSignatureType = 0;
        List<String> signatureTypes = new ArrayList<>();
        signatureTypes.add(String.format(Locale.getDefault(), "%s %d", mContext.getString(R.string.referee), 1));
        signatureTypes.add(String.format(Locale.getDefault(), "%s %d", mContext.getString(R.string.referee), 2));
        signatureTypes.add(mContext.getString(R.string.scorer));
        signatureTypes.add(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.captain), homeTeamName));
        signatureTypes.add(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.coach), homeTeamName));
        signatureTypes.add(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.captain), guestTeamName));
        signatureTypes.add(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.coach), guestTeamName));

        Spinner signatureTypeSpinner = mView.findViewById(R.id.signature_type_spinner);
        signatureTypeSpinner.setAdapter(new NameSpinnerAdapter<String>(mContext, mLayoutInflater, signatureTypes) {
            @Override
            public String getName(String item) {
                return item;
            }

            @Override
            public String getId(String item) {
                return item;
            }
        });
        signatureTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mSelectedSignatureType = index;
                mNameInputText.setHint(mContext.getString(R.string.name));

                if (mSelectedSignatureType == 3) {
                    mNameInputText.setText(homeCaptainName);
                } else if (mSelectedSignatureType == 5) {
                    mNameInputText.setText(guestCaptainName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        return new AlertDialog
                .Builder(mContext, R.style.AppTheme_Dialog)
                .setView(mView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> setSignature())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                .create();
    }

    private void setSignature() {
        Bitmap bitmap = mSignatureView.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        String base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
        String name = mNameInputText.getText().toString();

        switch (mSelectedSignatureType) {
            case 0:
                mScoreSheetActivity.getScoreSheetBuilder().setReferee1Signature(name, base64Image);
                break;
            case 1:
                mScoreSheetActivity.getScoreSheetBuilder().setReferee2Signature(name, base64Image);
                break;
            case 2:
                mScoreSheetActivity.getScoreSheetBuilder().setScorerSignature(name, base64Image);
                break;
            case 3:
                mScoreSheetActivity.getScoreSheetBuilder().setHomeCaptainSignature(name, base64Image);
                break;
            case 4:
                mScoreSheetActivity.getScoreSheetBuilder().setHomeCoachSignature(name, base64Image);
                break;
            case 5:
                mScoreSheetActivity.getScoreSheetBuilder().setGuestCaptainSignature(name, base64Image);
                break;
            case 6:
                mScoreSheetActivity.getScoreSheetBuilder().setGuestCoachSignature(name, base64Image);
                break;
        }
        mScoreSheetActivity.loadScoreSheet(true);
    }

}
