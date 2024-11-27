package com.tonkar.volleyballreferee.ui.data.game;

import android.content.Intent;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.*;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.*;
import com.tonkar.volleyballreferee.ui.scoresheet.ScoreSheetActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.text.DateFormat;
import java.util.*;

public abstract class StoredGameActivity extends AppCompatActivity {

    protected StoredGamesService mStoredGamesService;
    protected String             mGameId;
    protected IStoredGame        mStoredGame;

    public StoredGameActivity() {
        super();
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof StoredGameHandler storedGameHandler) {
                storedGameHandler.setStoredGame(mStoredGame);
            }
            if (fragment instanceof RulesHandler rulesHandler) {
                rulesHandler.setRules(mStoredGame.getRules());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_game, menu);

        MenuItem deleteMenu = menu.findItem(R.id.action_delete_game);
        String userId = PrefUtils.getUserId(this);
        deleteMenu.setVisible(Objects.equals(mStoredGame.getCreatedBy(), userId));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (itemId == R.id.action_delete_game) {
            deleteGame();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void updateGame() {
        TextView summaryText = findViewById(R.id.stored_game_summary);
        TextView dateText = findViewById(R.id.stored_game_date);
        TextView scoreText = findViewById(R.id.stored_game_score);
        ImageView kindItem = findViewById(R.id.game_kind_item);
        ImageView genderItem = findViewById(R.id.game_gender_item);
        TextView leagueText = findViewById(R.id.stored_game_league);

        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        summaryText.setText(String.format(Locale.getDefault(), "%s\t\t%d - %d\t\t%s", mStoredGame.getTeamName(TeamType.HOME),
                                          mStoredGame.getSets(TeamType.HOME), mStoredGame.getSets(TeamType.GUEST),
                                          mStoredGame.getTeamName(TeamType.GUEST)));
        dateText.setText(formatter.format(new Date(mStoredGame.getScheduledAt())));

        scoreText.setText(mStoredGame.getScore());

        switch (mStoredGame.getGender()) {
            case MIXED -> UiUtils.colorChipIcon(this, R.color.colorMixedLight, R.drawable.ic_mixed, genderItem);
            case LADIES -> UiUtils.colorChipIcon(this, R.color.colorLadiesLight, R.drawable.ic_ladies, genderItem);
            case GENTS -> UiUtils.colorChipIcon(this, R.color.colorGentsLight, R.drawable.ic_gents, genderItem);
        }

        switch (mStoredGame.getKind()) {
            case INDOOR_4X4 -> UiUtils.colorChipIcon(this, R.color.colorIndoor4x4Light, R.drawable.ic_4x4_small, kindItem);
            case BEACH -> UiUtils.colorChipIcon(this, R.color.colorBeachLight, R.drawable.ic_beach, kindItem);
            case SNOW -> UiUtils.colorChipIcon(this, R.color.colorSnowLight, R.drawable.ic_snow, kindItem);
            default -> UiUtils.colorChipIcon(this, R.color.colorIndoorLight, R.drawable.ic_6x6_small, kindItem);
        }

        if (mStoredGame.getLeague() != null && !mStoredGame.getLeague().getName().isEmpty()) {
            leagueText.setText(String.format(Locale.getDefault(), "%s / %s", mStoredGame.getLeague().getName(),
                                             mStoredGame.getLeague().getDivision()));
        }
        leagueText.setVisibility(mStoredGame.getLeague() == null || mStoredGame.getLeague().getName().isEmpty() ? View.GONE : View.VISIBLE);
    }

    protected void initScoreSheetAvailability() {
        View generateScoreSheetLayout = findViewById(R.id.generate_score_sheet_layout);
        generateScoreSheetLayout.setVisibility(View.VISIBLE);
    }

    protected void generateScoreSheet() {
        Log.i(Tags.STORED_GAMES, "Generate score sheet");
        Intent intent = new Intent(this, ScoreSheetActivity.class);
        intent.putExtra("game", mGameId);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    private void deleteGame() {
        Log.i(Tags.STORED_GAMES, "Delete game");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_game)).setMessage(getString(R.string.delete_game_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredGamesService.deleteGame(mGameId);
            UiUtils.makeText(StoredGameActivity.this, getString(R.string.deleted_game), Toast.LENGTH_LONG).show();
            UiUtils.navigateToMain(this, R.id.stored_games_list_fragment);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }
}
