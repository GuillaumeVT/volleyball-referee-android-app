package com.tonkar.volleyballreferee.ui.scoresheet;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.setup.NameSpinnerAdapter;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class SignatureDialogFragment extends DialogFragment {

    private Context            mContext;
    private LayoutInflater     mLayoutInflater;
    private View               mView;
    private EditText           mNameInputText;
    private SignatureView      mSignatureView;
    private int                mSelectedSignatureType;
    private ScoreSheetActivity mScoreSheetActivity;

    public static SignatureDialogFragment newInstance(IStoredGame storedGame) {
        SignatureDialogFragment fragment = new SignatureDialogFragment();
        Bundle args = new Bundle();
        args.putString("kind", storedGame.getKind().toString());
        args.putString("referee1Name", storedGame.getReferee1Name());
        args.putString("referee2Name", storedGame.getReferee2Name());
        args.putString("scorerName", storedGame.getScorerName());
        args.putString("homeTeamName", storedGame.getTeamName(TeamType.HOME));
        args.putString("guestTeamName", storedGame.getTeamName(TeamType.GUEST));
        args.putString("homeCaptainName", storedGame.getPlayerName(TeamType.HOME, storedGame.getCaptain(TeamType.HOME)));
        args.putString("guestCaptainName", storedGame.getPlayerName(TeamType.GUEST, storedGame.getCaptain(TeamType.GUEST)));
        args.putString("homeCoachName", storedGame.getCoachName(TeamType.HOME));
        args.putString("guestCoachName", storedGame.getCoachName(TeamType.GUEST));
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
        final Bundle bundle = requireArguments();
        GameType kind = GameType.valueOf(bundle.getString("kind"));
        String referee1Name = bundle.getString("referee1Name");
        String referee2Name = bundle.getString("referee2Name");
        String scorerName = bundle.getString("scorerName");
        String homeTeamName = bundle.getString("homeTeamName");
        String guestTeamName = bundle.getString("guestTeamName");
        String homeCaptainName = bundle.getString("homeCaptainName");
        String guestCaptainName = bundle.getString("guestCaptainName");
        String homeCoachName = bundle.getString("homeCoachName");
        String guestCoachName = bundle.getString("guestCoachName");

        mScoreSheetActivity = (ScoreSheetActivity) requireActivity();

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
        signatureTypes.add(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.captain), guestTeamName));
        if (GameType.INDOOR.equals(kind) || GameType.INDOOR_4X4.equals(kind)) {
            signatureTypes.add(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.coach), homeTeamName));
            signatureTypes.add(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.coach), guestTeamName));
        }

        Spinner signatureTypeSpinner = mView.findViewById(R.id.signature_type_spinner);
        signatureTypeSpinner.setAdapter(new NameSpinnerAdapter<>(mContext, mLayoutInflater, signatureTypes) {
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

                switch (mSelectedSignatureType) {
                    case 0 -> mNameInputText.setText(referee1Name);
                    case 1 -> mNameInputText.setText(referee2Name);
                    case 2 -> mNameInputText.setText(scorerName);
                    case 3 -> mNameInputText.setText(homeCaptainName);
                    case 4 -> mNameInputText.setText(guestCaptainName);
                    case 5 -> mNameInputText.setText(homeCoachName);
                    case 6 -> mNameInputText.setText(guestCoachName);
                    default -> {
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        return new AlertDialog.Builder(mContext, R.style.AppTheme_Dialog)
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
            case 0 -> mScoreSheetActivity.getScoreSheetBuilder().setReferee1Signature(name, base64Image);
            case 1 -> mScoreSheetActivity.getScoreSheetBuilder().setReferee2Signature(name, base64Image);
            case 2 -> mScoreSheetActivity.getScoreSheetBuilder().setScorerSignature(name, base64Image);
            case 3 -> mScoreSheetActivity.getScoreSheetBuilder().setHomeCaptainSignature(name, base64Image);
            case 4 -> mScoreSheetActivity.getScoreSheetBuilder().setGuestCaptainSignature(name, base64Image);
            case 5 -> mScoreSheetActivity.getScoreSheetBuilder().setHomeCoachSignature(name, base64Image);
            case 6 -> mScoreSheetActivity.getScoreSheetBuilder().setGuestCoachSignature(name, base64Image);
        }
        mScoreSheetActivity.loadScoreSheet(true);
    }

}
