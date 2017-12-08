package com.tonkar.volleyballreferee.ui.history;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

public abstract class RecentGameActivity extends AppCompatActivity {

    protected GamesHistoryService mGamesHistoryService;
    protected long                mGameDate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recent_game, menu);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            MenuItem shareMenu = menu.findItem(R.id.action_share_game);
            shareMenu.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    private void deleteGame() {
        Log.i("VBR-RecentActivity", "Delete game");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_game)).setMessage(getResources().getString(R.string.delete_game_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mGamesHistoryService.deleteRecordedGame(mGameDate);
                Toast.makeText(RecentGameActivity.this, getResources().getString(R.string.deleted_game), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(RecentGameActivity.this, RecentGamesListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void shareGame() {
        Log.i("VBR-RecentActivity", "Share game");
        String summary = mGamesHistoryService.getRecordedGameService(mGameDate).getGameSummary();
        UiUtils.shareScreen(this, getWindow(), summary);
    }

    protected void color(TextView textView, int color) {
        textView.setTextColor(UiUtils.getTextColor(this, color));
        textView.setBackgroundColor(color);
    }

    protected String buildScore(RecordedGameService recordedGameService) {
        StringBuilder builder = new StringBuilder();
        for (int setIndex = 0; setIndex < recordedGameService.getNumberOfSets(); setIndex++) {
            int homePoints = recordedGameService.getPoints(TeamType.HOME, setIndex);
            int guestPoints = recordedGameService.getPoints(TeamType.GUEST, setIndex);
            builder.append(String.valueOf(homePoints)).append('-').append(String.valueOf(guestPoints)).append("\t\t");
        }

        return builder.toString();
    }

}
