package com.tonkar.volleyballreferee.ui.game;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GeneralListener;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.score.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeBasedGameService;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class TimeBasedGameActivity extends AppCompatActivity implements GeneralListener, ScoreListener, TeamListener {

    private TimeBasedGameService mGameService;
    private RecordedGamesService mRecordedGamesService;
    private Random               mRandom;
    private TeamType             mTeamOnLeftSide;
    private TeamType             mTeamOnRightSide;
    private TextView             mLeftTeamNameText;
    private TextView             mRightTeamNameText;
    private FloatingActionButton mSwapTeamsButton;
    private MaterialButton       mLeftTeamScoreButton;
    private MaterialButton       mRightTeamScoreButton;
    private FloatingActionButton mLeftTeamServiceButton;
    private FloatingActionButton mRightTeamServiceButton;
    private FloatingActionButton mScoreRemoveButton;
    private TextView             mRemainingTimeText;
    private FloatingActionButton mStartMatchButton;
    private FloatingActionButton mStopMatchButton;
    private SimpleDateFormat     mTimeFormat;
    private CountDownTimer       mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(Tags.GAME_UI, "Create time-based game activity");
        setContentView(R.layout.activity_time_based_game);

        if (ServicesProvider.getInstance().isGameServiceUnavailable()) {
            ServicesProvider.getInstance().restoreGameService(getApplicationContext());
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean keepScreenOnSetting = sharedPreferences.getBoolean("pref_keep_screen_on", false);
        if (keepScreenOnSetting) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setTitle("");

        mGameService = (TimeBasedGameService) ServicesProvider.getInstance().getGameService();
        mRecordedGamesService = ServicesProvider.getInstance().getRecordedGamesService(getApplicationContext());

        if (mGameService == null || mRecordedGamesService == null) {
            UiUtils.navigateToHome(this);
        }

        mGameService.addGeneralListener(this);
        mGameService.addScoreListener(this);
        mGameService.addTeamListener(this);
        mRecordedGamesService.connectGameRecorder();

        mRandom = new Random();

        mLeftTeamNameText = findViewById(R.id.left_team_name_text);
        mRightTeamNameText = findViewById(R.id.right_team_name_text);

        mSwapTeamsButton = findViewById(R.id.swap_teams_button);

        mLeftTeamScoreButton = findViewById(R.id.left_team_score_button);
        mRightTeamScoreButton = findViewById(R.id.right_team_score_button);
        initButtonOnClickListeners();

        mLeftTeamServiceButton = findViewById(R.id.left_team_service_button);
        mRightTeamServiceButton = findViewById(R.id.right_team_service_button);

        mScoreRemoveButton = findViewById(R.id.score_remove_button);

        mTimeFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        mRemainingTimeText = findViewById(R.id.remaining_time_text);
        mStartMatchButton = findViewById(R.id.start_match_button);
        mStopMatchButton = findViewById(R.id.stop_match_button);

        UiUtils.fixFabCompatPadding(mSwapTeamsButton);
        UiUtils.fixFabCompatPadding(mLeftTeamServiceButton);
        UiUtils.fixFabCompatPadding(mRightTeamServiceButton);
        UiUtils.fixFabCompatPadding(mScoreRemoveButton);
        UiUtils.fixFabCompatPadding(mStartMatchButton);
        UiUtils.fixFabCompatPadding(mStopMatchButton);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, LaddersFragment.newInstance());
        fragmentTransaction.commit();


        mTeamOnLeftSide = mGameService.getTeamOnLeftSide();
        mTeamOnRightSide = mGameService.getTeamOnRightSide();
        onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, ActionOriginType.USER);
        onRemainingTimeUpdated();
        computeStartStopVisibility();

        if (mGameService.isMatchRunning()) {
            runMatch();
        } else  if (mGameService.isMatchStopped()) {
            disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mGameService.removeGeneralListener(this);
        mGameService.removeScoreListener(this);
        mGameService.removeTeamListener(this);
        mRecordedGamesService.disconnectGameRecorder(isFinishing());
    }

    @Override
    public void onBackPressed() {
        navigateToHomeWithDialog();
    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game, menu);

        if (mGameService.isMatchCompleted()) {
            MenuItem tossCoinMenu = menu.findItem(R.id.action_toss_coin);
            tossCoinMenu.setVisible(false);
        }

        MenuItem indexMenu = menu.findItem(R.id.action_index_game);

        if (PrefUtils.isSyncOn(this)) {
            if (mGameService.isIndexed()) {
                indexMenu.setIcon(R.drawable.ic_public);
            } else {
                indexMenu.setIcon(R.drawable.ic_private);
            }
        } else {
            indexMenu.setVisible(false);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            MenuItem shareMenu = menu.findItem(R.id.action_share);
            shareMenu.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toss_coin:
                tossACoin();
                return true;
            case R.id.action_navigate_home:
                navigateToHomeWithDialog();
                return true;
            case R.id.action_share:
                share();
                return true;
            case R.id.action_index_game:
                toggleIndexed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void tossACoin() {
        Log.i(Tags.GAME_UI, "Toss a coin");
        final String tossResult = mRandom.nextBoolean() ? getResources().getString(R.string.toss_heads) : getResources().getString(R.string.toss_tails);
        UiUtils.makeText(this, tossResult, Toast.LENGTH_LONG).show();
    }

    private void navigateToHomeWithDialog() {
        Log.i(Tags.GAME_UI, "Navigate to home");
        if (mGameService.isMatchCompleted()) {
            UiUtils.navigateToHome(this, false);
        } else {
            if (mGameService.getRemainingTime() > 0L) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
                builder.setTitle(getResources().getString(R.string.navigate_home)).setMessage(getResources().getString(R.string.navigate_home_question));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UiUtils.navigateToHome(TimeBasedGameActivity.this, false);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog alertDialog = builder.show();
                UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
                builder.setTitle(R.string.stop_match_description).setMessage(getResources().getString(R.string.confirm_stop_match_question));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(Tags.GAME_UI, "User accepts to stop");
                        mGameService.stop();
                        UiUtils.navigateToHome(TimeBasedGameActivity.this, false);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(Tags.GAME_UI, "User refuses to stop");
                    }
                });
                AlertDialog alertDialog = builder.show();
                UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
            }
        }
    }

    private void share() {
        Log.i(Tags.GAME_UI, "Share game");
        if (mGameService.isMatchCompleted()) {
            UiUtils.shareRecordedGame(this, mRecordedGamesService.getRecordedGameService(mGameService.getGameDate()));
        } else {
            UiUtils.shareGame(this, getWindow(), mGameService);
        }
    }

    private void toggleIndexed() {
        Log.i(Tags.GAME_UI, "Toggle indexed");
        mGameService.setIndexed(!mGameService.isIndexed());
    }

    // UI Callbacks

    private void initButtonOnClickListeners() {
        mLeftTeamScoreButton.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) { increaseLeftScore(null); }});
        mRightTeamScoreButton.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) { increaseRightScore(null); }});
    }

    public void swapTeams(View view) {
        Log.i(Tags.GAME_UI, "Swap teams");
        UiUtils.animate(this, mSwapTeamsButton);
        mGameService.swapTeams(ActionOriginType.USER);
    }

    public void swapFirstService(View view) {
        Log.i(Tags.GAME_UI, "Swap first service");
        mGameService.swapServiceAtStart();
    }

    public void removeLastPoint(View view) {
        Log.i(Tags.GAME_UI, "Remove last point");
        UiUtils.animate(this, mScoreRemoveButton);
        if (mGameService.isMatchRunning()) {
            mGameService.removeLastPoint();
        }
    }

    public void increaseLeftScore(View view) {
        Log.i(Tags.GAME_UI, "Increase left score");
        UiUtils.animate(this, mLeftTeamScoreButton);
        if (mGameService.isMatchRunning()) {
            mGameService.addPoint(mTeamOnLeftSide);
        }
    }

    public void increaseRightScore(View view) {
        Log.i(Tags.GAME_UI, "Increase right score");
        UiUtils.animate(this, mRightTeamScoreButton);
        if (mGameService.isMatchRunning()) {
            mGameService.addPoint(mTeamOnRightSide);
        }
    }

    public void startMatch(View view) {
        Log.i(Tags.GAME_UI, "Start match");
        UiUtils.animate(this, mStartMatchButton);
        if (!mGameService.isMatchStarted() && mGameService.getRemainingTime() > 0L) {
            startGameWithDialog();
        }
    }

    public void stopMatch(View view) {
        Log.i(Tags.GAME_UI, "Stop match");
        UiUtils.animate(this, mStopMatchButton);
        if (mGameService.isMatchRunning()) {
            stopGameWithDialog();
        }
    }

    private void startGameWithDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(R.string.start_match_description).setMessage(getResources().getString(R.string.confirm_start_match_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(Tags.GAME_UI, "User accepts to start");
                mGameService.start();
                computeStartStopVisibility();
                runMatch();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(Tags.GAME_UI, "User refuses to start");
            }
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void stopGameWithDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(R.string.stop_match_description).setMessage(getResources().getString(R.string.confirm_stop_match_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(Tags.GAME_UI, "User accepts to stop");
                mGameService.stop();
                mCountDownTimer.cancel();
                disableView();
                invalidateOptionsMenu();
                computeStartStopVisibility();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(Tags.GAME_UI, "User refuses to stop");
            }
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mTeamOnLeftSide = leftTeamType;
        mTeamOnRightSide = rightTeamType;

        // Left
        mLeftTeamNameText.setText(mGameService.getTeamName(mTeamOnLeftSide));
        UiUtils.colorTeamButton(this, mGameService.getTeamColor(mTeamOnLeftSide), mLeftTeamScoreButton);

        // Right

        mRightTeamNameText.setText(mGameService.getTeamName(mTeamOnRightSide));
        UiUtils.colorTeamButton(this, mGameService.getTeamColor(mTeamOnRightSide), mRightTeamScoreButton);

        onPointsUpdated(mTeamOnLeftSide, mGameService.getPoints(mTeamOnLeftSide));
        onPointsUpdated(mTeamOnRightSide, mGameService.getPoints(mTeamOnRightSide));
        onServiceSwapped(mGameService.getServingTeam());

        UiUtils.animateBounce(this, mLeftTeamNameText);
        UiUtils.animateBounce(this, mRightTeamNameText);
        UiUtils.animateBounce(this, mLeftTeamScoreButton);
        UiUtils.animateBounce(this, mRightTeamScoreButton);

        if (ActionOriginType.APPLICATION.equals(actionOriginType)) {
            UiUtils.makeText(this, getResources().getString(R.string.switch_sides), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {}

    @Override
    public void onTeamRotated(TeamType teamType) {}

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        if (mTeamOnLeftSide.equals(teamType)) {
            mLeftTeamScoreButton.setText(String.valueOf(newCount));
        } else {
            mRightTeamScoreButton.setText(String.valueOf(newCount));
        }
    }

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onServiceSwapped(TeamType teamType) {
        if (mTeamOnLeftSide.equals(teamType)) {
            mLeftTeamServiceButton.show();
            mRightTeamServiceButton.hide();
        } else {
            mRightTeamServiceButton.show();
            mLeftTeamServiceButton.hide();
        }
    }

    @Override
    public void onSetStarted() {}

    @Override
    public void onSetCompleted() {}

    @Override
    public void onMatchCompleted(final TeamType winner) {
        UiUtils.makeText(this, String.format(getResources().getString(R.string.won_game), mGameService.getTeamName(winner)), Toast.LENGTH_LONG).show();
    }

    private void onRemainingTimeUpdated() {
        long remainingTime = mGameService.getRemainingTime();

        if (remainingTime < 0L) {
            remainingTime = 0L;
        }

        mRemainingTimeText.setText(mTimeFormat.format(new Date(remainingTime)));
    }

    private void disableView() {
        Log.i(Tags.GAME_UI, "Disable game activity view");
        mSwapTeamsButton.setEnabled(false);
        mLeftTeamScoreButton.setEnabled(false);
        mRightTeamScoreButton.setEnabled(false);
        mScoreRemoveButton.setEnabled(false);
    }

    private void computeStartStopVisibility() {
        if (mGameService.isMatchStarted()) {
            mStartMatchButton.hide();
        } else {
            mStartMatchButton.show();
        }
        if (mGameService.isMatchRunning()) {
            mStopMatchButton.show();
        } else {
            mStopMatchButton.hide();
        }
    }

    private void runMatch() {
        mCountDownTimer = new CountDownTimer(mGameService.getRemainingTime(), 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                mRemainingTimeText.setText(mTimeFormat.format(new Date(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                UiUtils.playNotificationSound(TimeBasedGameActivity.this);
            }
        };

        mCountDownTimer.start();
    }

    @Override
    public void onMatchIndexed(boolean indexed) {
        if (indexed) {
            UiUtils.makeText(this, getResources().getString(R.string.public_game_message), Toast.LENGTH_LONG).show();
        } else {
            UiUtils.makeText(this, getResources().getString(R.string.private_game_message), Toast.LENGTH_LONG).show();
        }
        invalidateOptionsMenu();
    }

}
