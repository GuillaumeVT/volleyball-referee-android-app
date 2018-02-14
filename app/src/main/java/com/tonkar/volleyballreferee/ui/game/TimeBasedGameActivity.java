package com.tonkar.volleyballreferee.ui.game;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.score.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeBasedGameService;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class TimeBasedGameActivity extends AppCompatActivity implements ScoreListener, TeamListener {

    private TimeBasedGameService mGameService;
    private RecordedGamesService mRecordedGamesService;
    private Random               mRandom;
    private TeamType             mTeamOnLeftSide;
    private TeamType             mTeamOnRightSide;
    private TextView             mLeftTeamNameText;
    private TextView             mRightTeamNameText;
    private ImageButton          mSwapTeamsButton;
    private Button               mLeftTeamScoreButton;
    private Button               mRightTeamScoreButton;
    private ImageButton          mLeftTeamServiceButton;
    private ImageButton          mRightTeamServiceButton;
    private ImageButton          mScoreRemoveButton;
    private TextView             mRemainingTimeText;
    private ImageButton          mStartMatchButton;
    private ImageButton          mStopMatchButton;
    private SimpleDateFormat     mTimeFormat;
    private CountDownTimer       mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-TBGameActivity", "Create time-based game activity");
        setContentView(R.layout.activity_time_based_game);

        if (!ServicesProvider.getInstance().areServicesAvailable()) {
            ServicesProvider.getInstance().restoreGameService(getApplicationContext());
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean keepScreenOnSetting = sharedPreferences.getBoolean("pref_keep_screen_on", false);
        if (keepScreenOnSetting) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setTitle("");

        mGameService = (TimeBasedGameService) ServicesProvider.getInstance().getGameService();
        mRecordedGamesService = ServicesProvider.getInstance().getRecordedGamesService();

        mGameService.addScoreListener(this);
        mGameService.addTeamListener(this);
        mRecordedGamesService.connectGameRecorder();

        mRandom = new Random();

        mLeftTeamNameText = findViewById(R.id.left_team_name_text);
        mRightTeamNameText = findViewById(R.id.right_team_name_text);

        mSwapTeamsButton = findViewById(R.id.swap_teams_button);

        mLeftTeamScoreButton = findViewById(R.id.left_team_score_button);
        mRightTeamScoreButton = findViewById(R.id.right_team_score_button);

        mLeftTeamServiceButton = findViewById(R.id.left_team_service_button);
        mRightTeamServiceButton = findViewById(R.id.right_team_service_button);

        mScoreRemoveButton = findViewById(R.id.score_remove_button);

        mTimeFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        mRemainingTimeText = findViewById(R.id.remaining_time_text);
        mStartMatchButton = findViewById(R.id.start_match_button);
        mStopMatchButton = findViewById(R.id.stop_match_button);

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

        MenuItem recordMenu = menu.findItem(R.id.action_record_game);

        if (mGameService.isMatchCompleted()) {
            MenuItem tossCoinMenu = menu.findItem(R.id.action_toss_coin);
            tossCoinMenu.setVisible(false);
            recordMenu.setVisible(false);
        } else {
            if (PrefUtils.isPrefOnlineRecordingEnabled(this)) {
                if (mRecordedGamesService.isOnlineRecordingEnabled()) {
                    recordMenu.setIcon(R.drawable.ic_record_on_menu);
                } else {
                    recordMenu.setIcon(R.drawable.ic_record_off_menu);
                }
            } else {
                recordMenu.setVisible(false);
            }
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
            case R.id.action_record_game:
                toggleOnlineRecording();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void tossACoin() {
        Log.i("VBR-TBGameActivity", "Toss a coin");
        final String tossResult = mRandom.nextBoolean() ? getResources().getString(R.string.toss_heads) : getResources().getString(R.string.toss_tails);
        Toast.makeText(this, tossResult, Toast.LENGTH_LONG).show();
    }

    private void navigateToHomeWithDialog() {
        Log.i("VBR-TBGameActivity", "Navigate to home");
        if (mGameService.isMatchCompleted()) {
            navigateToHome();
        } else {
            if (mGameService.getRemainingTime() > 0L) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
                builder.setTitle(getResources().getString(R.string.navigate_home)).setMessage(getResources().getString(R.string.navigate_home_question));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        navigateToHome();
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
                        Log.i("VBR-TBGameActivity", "User accepts to stop");
                        mGameService.stop();
                        navigateToHome();
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("VBR-TBGameActivity", "User refuses to stop");
                    }
                });
                AlertDialog alertDialog = builder.show();
                UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
            }
        }
    }

    private void navigateToHome() {
        UiUtils.navigateToHome(this, false);
    }

    private void share() {
        Log.i("VBR-TBGameActivity", "Share game");
        if (mGameService.isMatchCompleted()) {
            UiUtils.shareRecordedGame(this, mRecordedGamesService.getRecordedGameService(mGameService.getGameDate()));
        } else {
            UiUtils.shareGame(this, getWindow(), mGameService, mRecordedGamesService.isOnlineRecordingEnabled());
        }
    }

    private void toggleOnlineRecording() {
        Log.i("VBR-GameActivity", "Toggle online recording");
        mRecordedGamesService.toggleOnlineRecording();
        if (mRecordedGamesService.isOnlineRecordingEnabled()) {
            Toast.makeText(this, getResources().getString(R.string.stream_online_on_message), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.stream_online_off_message), Toast.LENGTH_LONG).show();
        }
        invalidateOptionsMenu();
    }

    // UI Callbacks

    public void swapTeams(View view) {
        Log.i("VBR-TBGameActivity", "Swap teams");
        UiUtils.animate(this, mSwapTeamsButton);
        mGameService.swapTeams(ActionOriginType.USER);
    }

    public void swapFirstService(View view) {
        Log.i("VBR-TBGameActivity", "Swap first service");
        mGameService.swapServiceAtStart();
    }

    public void removeLastPoint(View view) {
        Log.i("VBR-TBGameActivity", "Remove last point");
        UiUtils.animate(this, mScoreRemoveButton);
        if (mGameService.isMatchRunning()) {
            mGameService.removeLastPoint();
        }
    }

    public void increaseLeftScore(View view) {
        Log.i("VBR-TBGameActivity", "Increase left score");
        UiUtils.animate(this, mLeftTeamScoreButton);
        if (mGameService.isMatchRunning()) {
            mGameService.addPoint(mTeamOnLeftSide);
        }
    }

    public void increaseRightScore(View view) {
        Log.i("VBR-TBGameActivity", "Increase right score");
        UiUtils.animate(this, mRightTeamScoreButton);
        if (mGameService.isMatchRunning()) {
            mGameService.addPoint(mTeamOnRightSide);
        }
    }

    public void startMatch(View view) {
        Log.i("VBR-TBGameActivity", "Start match");
        UiUtils.animate(this, mStartMatchButton);
        if (!mGameService.isMatchStarted() && mGameService.getRemainingTime() > 0L) {
            startGameWithDialog();
        }
    }

    public void stopMatch(View view) {
        Log.i("VBR-TBGameActivity", "Stop match");
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
                Log.i("VBR-TBGameActivity", "User accepts to start");
                mGameService.start();
                computeStartStopVisibility();
                runMatch();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i("VBR-TBGameActivity", "User refuses to start");
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
                Log.i("VBR-TBGameActivity", "User accepts to stop");
                mGameService.stop();
                mCountDownTimer.cancel();
                disableView();
                invalidateOptionsMenu();
                computeStartStopVisibility();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i("VBR-TBGameActivity", "User refuses to stop");
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
            Toast.makeText(this, getResources().getString(R.string.switch_sides), Toast.LENGTH_LONG).show();
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
            mLeftTeamServiceButton.setVisibility(View.VISIBLE);
            mRightTeamServiceButton.setVisibility(View.INVISIBLE);
        } else {
            mRightTeamServiceButton.setVisibility(View.VISIBLE);
            mLeftTeamServiceButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onSetStarted() {}

    @Override
    public void onSetCompleted() {}

    @Override
    public void onMatchCompleted(final TeamType winner) {
        Toast.makeText(this, String.format(getResources().getString(R.string.won_game), mGameService.getTeamName(winner)), Toast.LENGTH_LONG).show();
    }

    private void onRemainingTimeUpdated() {
        long remainingTime = mGameService.getRemainingTime();

        if (remainingTime < 0L) {
            remainingTime = 0L;
        }

        mRemainingTimeText.setText(mTimeFormat.format(new Date(remainingTime)));
    }

    private void disableView() {
        Log.i("VBR-GameActivity", "Disable game activity view");
        mSwapTeamsButton.setEnabled(false);
        mLeftTeamScoreButton.setEnabled(false);
        mRightTeamScoreButton.setEnabled(false);
        mScoreRemoveButton.setEnabled(false);
    }

    private void computeStartStopVisibility() {
        mStartMatchButton.setVisibility(mGameService.isMatchStarted() ? View.INVISIBLE : View.VISIBLE);
        mStopMatchButton.setVisibility(mGameService.isMatchRunning()? View.VISIBLE : View.INVISIBLE);
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

}
