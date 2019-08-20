package com.tonkar.volleyballreferee.ui.stored.game;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.stored.DataSynchronizationListener;
import com.tonkar.volleyballreferee.engine.stored.IStoredGame;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesService;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.RulesHandler;
import com.tonkar.volleyballreferee.ui.interfaces.StoredGameHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

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
            MenuItem shareMenu = menu.findItem(R.id.action_share_game_score_sheet);
            shareMenu.setVisible(false);
        }

        MenuItem recordMenu = menu.findItem(R.id.action_index_game);
        MenuItem deleteMenu = menu.findItem(R.id.action_delete_game);
        MenuItem shareLinkMenu = menu.findItem(R.id.action_share_game_link);

        String userId = PrefUtils.getUser(this).getId();
        boolean canSync = PrefUtils.canSync(this);

        if (canSync && mStoredGame.getCreatedBy().equals(userId)) {
            if (mStoredGamesService.isGameIndexed(mGameId)) {
                recordMenu.setIcon(R.drawable.ic_public_menu);
            } else {
                recordMenu.setIcon(R.drawable.ic_private_menu);
            }
        } else {
            recordMenu.setVisible(false);
        }

        deleteMenu.setVisible(mStoredGame.getCreatedBy().equals(userId));
        shareLinkMenu.setVisible(canSync);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_index_game:
                toggleGameIndexed();
                return true;
            case R.id.action_delete_game:
                deleteGame();
                return true;
            case R.id.action_share_game_score_sheet:
                shareGameScoreSheet();
                return true;
            case R.id.action_share_game_link:
                shareGameLink();
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
                UiUtils.colorChipIcon(this, R.color.colorMixedLight, R.drawable.ic_mixed, genderItem);
                break;
            case LADIES:
                UiUtils.colorChipIcon(this, R.color.colorLadiesLight, R.drawable.ic_ladies, genderItem);
                break;
            case GENTS:
                UiUtils.colorChipIcon(this, R.color.colorGentsLight, R.drawable.ic_gents, genderItem);
                break;
        }

        switch (mStoredGame.getKind()) {
            case INDOOR_4X4:
                UiUtils.colorChipIcon(this, R.color.colorIndoor4x4Light, R.drawable.ic_4x4_small, kindItem);
                break;
            case BEACH:
                UiUtils.colorChipIcon(this, R.color.colorBeachLight, R.drawable.ic_beach, kindItem);
                break;
            case TIME:
                UiUtils.colorChipIcon(this, R.color.colorTimeLight, R.drawable.ic_time_based, kindItem);
                break;
            case INDOOR:
            default:
                UiUtils.colorChipIcon(this, R.color.colorIndoorLight, R.drawable.ic_6x6_small, kindItem);
                break;
        }

        if (PrefUtils.canSync(this)) {
            if (mStoredGame.isIndexed()) {
                UiUtils.colorChipIcon(this, R.color.colorWebPublicLight, R.drawable.ic_public, indexedItem);
            } else {
                UiUtils.colorChipIcon(this, R.color.colorWebPrivateLight, R.drawable.ic_private, indexedItem);
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

    private void shareGameScoreSheet() {
        Log.i(Tags.STORED_GAMES, "Share game score sheet");
        UiUtils.shareStoredGame(this, mStoredGamesService.getGame(mGameId));
    }

    private void shareGameLink() {
        Log.i(Tags.STORED_GAMES, "Share game link");
        UiUtils.shareGameLink(this, mStoredGamesService.getGame(mGameId));
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof StoredGameHandler) {
            StoredGameHandler storedGameHandler = (StoredGameHandler) fragment;
            storedGameHandler.setStoredGame(mStoredGame);
        }
        if (fragment instanceof RulesHandler) {
            RulesHandler rulesHandler = (RulesHandler) fragment;
            rulesHandler.setRules(mStoredGame.getRules());
        }
    }
}
