package com.tonkar.volleyballreferee.ui.stored;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.stored.DataSynchronizationListener;
import com.tonkar.volleyballreferee.engine.stored.IStoredGame;
import com.tonkar.volleyballreferee.engine.stored.ScoreSheetWriter;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesService;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.StoredGameHandler;
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
    protected IStoredGame        mStoredGame;

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

        if (PrefUtils.canSync(this) && mStoredGame.getCreatedBy().equals(PrefUtils.getUser(this).getId())) {
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
        Chip kindItem = findViewById(R.id.game_kind_item);
        Chip genderItem = findViewById(R.id.game_gender_item);
        Chip indexedItem = findViewById(R.id.game_indexed_item);
        TextView leagueText = findViewById(R.id.stored_game_league);

        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        summaryText.setText(String.format(Locale.getDefault(),"%s\t\t%d - %d\t\t%s",
                mStoredGame.getTeamName(TeamType.HOME), mStoredGame.getSets(TeamType.HOME), mStoredGame.getSets(TeamType.GUEST), mStoredGame.getTeamName(TeamType.GUEST)));
        dateText.setText(formatter.format(new Date(mStoredGame.getScheduledAt())));

        scoreText.setText(mStoredGame.getScore());

        switch (mStoredGame.getGender()) {
            case MIXED:
                genderItem.setChipIconResource(R.drawable.ic_mixed);
                genderItem.setChipBackgroundColorResource(R.color.colorMixedLight);
                genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                genderItem.setChipIconResource(R.drawable.ic_ladies);
                genderItem.setChipBackgroundColorResource(R.color.colorLadiesLight);
                genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                genderItem.setChipIconResource(R.drawable.ic_gents);
                genderItem.setChipBackgroundColorResource(R.color.colorGentsLight);
                genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
        }

        switch (mStoredGame.getKind()) {
            case INDOOR_4X4:
                kindItem.setChipIconResource(R.drawable.ic_4x4_small);
                kindItem.setChipBackgroundColorResource(R.color.colorIndoor4x4Light);
                kindItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case BEACH:
                kindItem.setChipIconResource(R.drawable.ic_beach);
                kindItem.setChipBackgroundColorResource(R.color.colorBeachLight);
                kindItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case TIME:
                kindItem.setChipIconResource(R.drawable.ic_time_based);
                kindItem.setChipBackgroundColorResource(R.color.colorTimeLight);
                kindItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case INDOOR:
            default:
                kindItem.setChipIconResource(R.drawable.ic_6x6_small);
                kindItem.setChipBackgroundColorResource(R.color.colorIndoorLight);
                kindItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
        }

        if (PrefUtils.canSync(this)) {
            if (mStoredGame.isIndexed()) {
                indexedItem.setChipIconResource(R.drawable.ic_public);
                indexedItem.setChipBackgroundColorResource(R.color.colorWebPublicLight);
                indexedItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
            } else {
                indexedItem.setChipIconResource(R.drawable.ic_private);
                indexedItem.setChipBackgroundColorResource(R.color.colorWebPrivateLight);
                indexedItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
            }
            indexedItem.setVisibility(View.VISIBLE);
        } else {
            indexedItem.setVisibility(View.GONE);
        }

        if (mStoredGame.getLeague() != null && !mStoredGame.getLeague().getName().isEmpty()) {
            leagueText.setText(String.format(Locale.getDefault(), "%s / %s" , mStoredGame.getLeague().getName(), mStoredGame.getLeague().getDivision()));
        }
        leagueText.setVisibility(mStoredGame.getLeague() == null || mStoredGame.getLeague().getName().isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void toggleGameIndexed() {
        Log.i(Tags.STORED_GAMES, "Toggle game indexed");
        if (PrefUtils.canSync(this)) {
            mStoredGamesService.toggleGameIndexed(mGameId, new DataSynchronizationListener() {
                @Override
                public void onSynchronizationSucceeded() {
                    runOnUiThread(() -> updateGame());
                }

                @Override
                public void onSynchronizationFailed() {
                    runOnUiThread(() -> UiUtils.makeErrorText(StoredGameActivity.this, getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show());
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
        if (fragment instanceof StoredGameHandler) {
            StoredGameHandler storedGameHandler = (StoredGameHandler) fragment;
            storedGameHandler.setStoredGame(mStoredGame);
        }
    }
}
