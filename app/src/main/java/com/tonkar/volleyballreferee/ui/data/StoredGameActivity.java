package com.tonkar.volleyballreferee.ui.data;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.ScoreSheetWriter;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredGamesService;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.StoredGameServiceHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class StoredGameActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    protected StoredGamesService mStoredGamesService;
    protected String             mGameId;
    protected StoredGameService  mStoredGameService;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_game, menu);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            MenuItem shareMenu = menu.findItem(R.id.action_share_game);
            shareMenu.setVisible(false);

            MenuItem scoreSheetMenu = menu.findItem(R.id.action_generate_score_sheet);
            scoreSheetMenu.setVisible(false);
        }

        MenuItem recordMenu = menu.findItem(R.id.action_index_game);
        MenuItem deleteMenu = menu.findItem(R.id.action_delete_game);

        if (PrefUtils.canSync(this) && mStoredGameService.getCreatedBy().equals(PrefUtils.getAuthentication(this).getUserId())) {
            if (mStoredGamesService.isGameIndexed(mGameId)) {
                recordMenu.setIcon(R.drawable.ic_public_menu);
            } else {
                recordMenu.setIcon(R.drawable.ic_private_menu);
            }
        } else {
            recordMenu.setVisible(false);
            deleteMenu.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_index_game:
                toggleGameIndexed();
                return true;
            case R.id.action_generate_score_sheet:
                generateScoreSheet();
                return true;
            case R.id.action_delete_game:
                deleteGame();
                return true;
            case R.id.action_share_game:
                shareGame();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    protected void updateGame() {
        TextView summaryText = findViewById(R.id.stored_game_summary);
        TextView dateText = findViewById(R.id.stored_game_date);
        TextView scoreText = findViewById(R.id.stored_game_score);
        ImageView genderTypeImage = findViewById(R.id.stored_game_gender_image);
        ImageView gameTypeImage = findViewById(R.id.stored_game_type_image);
        ImageView indexedImage = findViewById(R.id.stored_game_indexed_image);
        TextView leagueText = findViewById(R.id.stored_game_league);

        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        summaryText.setText(String.format(Locale.getDefault(),"%s\t\t%d - %d\t\t%s",
                mStoredGameService.getTeamName(TeamType.HOME), mStoredGameService.getSets(TeamType.HOME), mStoredGameService.getSets(TeamType.GUEST), mStoredGameService.getTeamName(TeamType.GUEST)));
        dateText.setText(formatter.format(new Date(mStoredGameService.getScheduledAt())));

        scoreText.setText(mStoredGameService.getScore());

        switch (mStoredGameService.getGender()) {
            case MIXED:
                genderTypeImage.setImageResource(R.drawable.ic_mixed);
                genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                genderTypeImage.setImageResource(R.drawable.ic_ladies);
                genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                genderTypeImage.setImageResource(R.drawable.ic_gents);
                genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorGents), PorterDuff.Mode.SRC_IN));
                break;
        }

        switch (mStoredGameService.getKind()) {
            case INDOOR_4X4:
                gameTypeImage.setImageResource(R.drawable.ic_4x4);
                gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorIndoor4x4), PorterDuff.Mode.SRC_IN));
                break;
            case BEACH:
                gameTypeImage.setImageResource(R.drawable.ic_sun);
                gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorBeach), PorterDuff.Mode.SRC_IN));
                break;
            case TIME:
                gameTypeImage.setImageResource(R.drawable.ic_time_based);
                gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorTime), PorterDuff.Mode.SRC_IN));
                break;
            case INDOOR:
            default:
                gameTypeImage.setImageResource(R.drawable.ic_6x6);
                gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorIndoor), PorterDuff.Mode.SRC_IN));
                break;
        }

        if (mStoredGameService.isIndexed()) {
            indexedImage.setImageResource(R.drawable.ic_public);
            indexedImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorWeb), PorterDuff.Mode.SRC_IN));
        } else {
            indexedImage.setImageResource(R.drawable.ic_private);
            indexedImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark), PorterDuff.Mode.SRC_IN));
        }

        indexedImage.setVisibility(PrefUtils.canSync(this) ? View.VISIBLE : View.GONE);

        if (mStoredGameService.getLeague() != null && !mStoredGameService.getLeague().getName().isEmpty()) {
            leagueText.setText(String.format(Locale.getDefault(), "%s / %s" , mStoredGameService.getLeague().getName(), mStoredGameService.getLeague().getDivision()));
        }
        leagueText.setVisibility(mStoredGameService.getLeague() == null || mStoredGameService.getLeague().getName().isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void toggleGameIndexed() {
        Log.i(Tags.STORED_GAMES, "Toggle game indexed");
        if (PrefUtils.canSync(this)) {
            mStoredGamesService.toggleGameIndexed(mGameId, new DataSynchronizationListener() {
                @Override
                public void onSynchronizationSucceeded() {
                    updateGame();
                }

                @Override
                public void onSynchronizationFailed() {
                    UiUtils.makeErrorText(StoredGameActivity.this, getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
                }
            });
            invalidateOptionsMenu();
        }
    }

    private void generateScoreSheet() {
        Log.i(Tags.STORED_GAMES, "Generate score sheet");
        File file = ScoreSheetWriter.writeStoredGame(this, mStoredGamesService.getGame(mGameId));
        if (file == null) {
            UiUtils.makeErrorText(this, getString(R.string.report_exception), Toast.LENGTH_LONG).show();
        } else {
            Uri uri = FileProvider.getUriForFile(this, "com.tonkar.volleyballreferee.fileprovider", file);
            grantUriPermission("com.tonkar.volleyballreferee.fileprovider", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(uri,"text/html");
            target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent intent = Intent.createChooser(target, file.getName());
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(Tags.STORED_GAMES, "Exception while showing report", e);
                UiUtils.makeErrorText(this, getString(R.string.report_exception), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void deleteGame() {
        Log.i(Tags.STORED_GAMES, "Delete game");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_game)).setMessage(getString(R.string.delete_game_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredGamesService.deleteGame(mGameId);
            UiUtils.makeText(StoredGameActivity.this, getString(R.string.deleted_game), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(StoredGameActivity.this, StoredGamesListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void shareGame() {
        Log.i(Tags.STORED_GAMES, "Share game");
        UiUtils.shareStoredGame(this, mStoredGamesService.getGame(mGameId));
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof StoredGameServiceHandler) {
            StoredGameServiceHandler storedGameServiceHandler = (StoredGameServiceHandler) fragment;
            storedGameServiceHandler.setStoredGameService(mStoredGameService);
        }
    }
}
