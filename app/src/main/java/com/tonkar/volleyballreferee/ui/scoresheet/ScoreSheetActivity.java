package com.tonkar.volleyballreferee.ui.scoresheet;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.print.ScoreSheetPdfConverter;
import android.provider.MediaStore;
import android.util.*;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.scoresheet.ScoreSheetBuilder;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.util.*;

import java.io.*;

public class ScoreSheetActivity extends ProgressIndicatorActivity {

    private IStoredGame       mStoredGame;
    private ScoreSheetBuilder mScoreSheetBuilder;
    private WebView           mWebView;

    private ActivityResultLauncher<Intent> mSelectScoreSheetLogoResultLauncher;
    private ActivityResultLauncher<Intent> mCreatePdfScoreSheetResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String gameId = getIntent().getStringExtra("game");
        StoredGamesService storedGamesService = new StoredGamesManager(this);
        mStoredGame = storedGamesService.getGame(gameId);

        if (mStoredGame == null) {
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            mScoreSheetBuilder = new ScoreSheetBuilder(this, mStoredGame);

            super.onCreate(savedInstanceState);

            Log.i(Tags.SCORE_SHEET, "Create score sheet activity");
            setContentView(R.layout.activity_score_sheet);

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("");
            UiUtils.updateToolbarLogo(toolbar, mStoredGame.getKind(), UsageType.NORMAL);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }

            mSyncLayout = findViewById(R.id.score_sheet_sync_layout);
            mSyncLayout.setEnabled(false);

            mWebView = findViewById(R.id.score_sheet);

            loadScoreSheet(false);

            FloatingActionButton logoButton = findViewById(R.id.score_sheet_logo_button);
            logoButton.setOnClickListener(v -> selectScoreSheetLogo());

            FloatingActionButton signatureButton = findViewById(R.id.sign_score_sheet_button);
            signatureButton.setOnClickListener(v -> showSignatureDialog());

            FloatingActionButton observationButton = findViewById(R.id.score_sheet_observation_button);
            observationButton.setOnClickListener(v -> showObservationDialog());

            FloatingActionButton saveButton = findViewById(R.id.save_score_sheet_button);
            saveButton.setOnClickListener(v -> createPdfScoreSheet());

            mSelectScoreSheetLogoResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                                                                            result -> {
                                                                                if (result.getResultCode() == Activity.RESULT_OK) {
                                                                                    // There are no request codes
                                                                                    Intent data = result.getData();
                                                                                    if (data != null) {
                                                                                        try {
                                                                                            Uri imageUri = data.getData();
                                                                                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                                                                                    getContentResolver(), imageUri);
                                                                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20,
                                                                                                            stream);

                                                                                            String base64Image = Base64.encodeToString(
                                                                                                    stream.toByteArray(), Base64.NO_WRAP);
                                                                                            mScoreSheetBuilder.setLogo(base64Image);
                                                                                            loadScoreSheet(false);

                                                                                        } catch (IOException e) {
                                                                                            Log.e(Tags.SCORE_SHEET,
                                                                                                  "Exception while opening the logo", e);
                                                                                        }
                                                                                    }
                                                                                }
                                                                            });

            mCreatePdfScoreSheetResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    if (data != null) {
                        ScoreSheetPdfConverter scoreSheetPdfConverter = new ScoreSheetPdfConverter(ScoreSheetActivity.this, data.getData());
                        scoreSheetPdfConverter.convert(mScoreSheetBuilder.createScoreSheet());
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectScoreSheetLogo() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("image/*");

        Intent intent = Intent.createChooser(chooseFile, "");
        mSelectScoreSheetLogoResultLauncher.launch(intent);
    }

    private void showSignatureDialog() {
        SignatureDialogFragment signatureDialogFragment = SignatureDialogFragment.newInstance(mStoredGame);
        signatureDialogFragment.show(getSupportFragmentManager(), "signature_dialog");
    }

    private void showObservationDialog() {
        RemarksDialogFragment remarksDialogFragment = RemarksDialogFragment.newInstance();
        remarksDialogFragment.show(getSupportFragmentManager(), "remarks_dialog");
    }

    private void createPdfScoreSheet() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, mScoreSheetBuilder.getFilename().replace(".html", ".pdf"));

        mCreatePdfScoreSheetResultLauncher.launch(intent);
    }

    void loadScoreSheet(boolean scrollBottom) {
        ScoreSheetBuilder.ScoreSheet scoreSheet = mScoreSheetBuilder.createScoreSheet();
        mWebView.loadDataWithBaseURL(null, scoreSheet.content(), "text/html", "UTF-8", null);
        if (scrollBottom) {
            mWebView.pageDown(true);
        }
    }

    ScoreSheetBuilder getScoreSheetBuilder() {
        return mScoreSheetBuilder;
    }
}
