package com.tonkar.volleyballreferee.ui.history;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.game.SetsListAdapter;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RecentGameActivity extends AppCompatActivity {

    private GamesHistoryService mGamesHistoryService;
    private long                mGameDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_game);

        Log.i("VBR-RecentActivity", "Create recent game activity");

        Intent intent = getIntent();
        mGameDate = intent.getLongExtra("game_date", 0L);

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        setTitle(formatter.format(new Date(mGameDate)));

        mGamesHistoryService = ServicesProvider.getInstance().getGameHistoryService();

        RecordedGameService recordedGameService = mGamesHistoryService.getRecordedGameService(mGameDate);

        TextView homeTeamName = findViewById(R.id.home_team_name_text);
        TextView guestTeamName = findViewById(R.id.guest_team_name_text);
        TextView homeTeamSets = findViewById(R.id.home_team_set_text);
        TextView guestTeamSets = findViewById(R.id.guest_team_set_text);

        homeTeamName.setText(recordedGameService.getTeamName(TeamType.HOME));
        guestTeamName.setText(recordedGameService.getTeamName(TeamType.GUEST));
        homeTeamSets.setText(String.valueOf(recordedGameService.getSets(TeamType.HOME)));
        guestTeamSets.setText(String.valueOf(recordedGameService.getSets(TeamType.GUEST)));

        color(homeTeamSets, recordedGameService.getTeamColor(TeamType.HOME));
        color(guestTeamSets, recordedGameService.getTeamColor(TeamType.GUEST));

        ListView setsList = findViewById(R.id.recent_game_set_list);
        SetsListAdapter setsListAdapter = new SetsListAdapter(getLayoutInflater(), recordedGameService, recordedGameService, false);
        setsList.setAdapter(setsListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recent_game, menu);
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
        builder.show();
    }

    private void shareGame() {
        Log.i("VBR-RecentActivity", "Share game");
        String summary = mGamesHistoryService.getRecordedGameService(mGameDate).getGameSummary();
        UiUtils.shareScreen(this, getWindow(), summary);
    }

    private void color(TextView textView, int colorId) {
        int backgroundColor = ContextCompat.getColor(this, colorId);
        textView.setTextColor(UiUtils.getTextColor(this, backgroundColor));
        textView.setBackgroundColor(backgroundColor);
    }

}
