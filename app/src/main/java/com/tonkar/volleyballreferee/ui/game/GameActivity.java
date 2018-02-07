package com.tonkar.volleyballreferee.ui.game;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeoutListener;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.Random;

public class GameActivity extends AppCompatActivity implements ScoreListener, TimeoutListener, TeamListener {

    private GameService          mGameService;
    private RecordedGamesService mRecordedGamesService;
    private Random               mRandom;
    private TeamType             mTeamOnLeftSide;
    private TeamType             mTeamOnRightSide;
    private TextView             mLeftTeamNameText;
    private TextView             mRightTeamNameText;
    private ImageButton          mSwapTeamsButton;
    private Button               mLeftTeamScoreButton;
    private Button               mRightTeamScoreButton;
    private TextView             mLeftTeamSetsText;
    private TextView             mRightTeamSetsText;
    private ImageButton          mLeftTeamServiceButton;
    private ImageButton          mRightTeamServiceButton;
    private TextView             mSetsText;
    private ImageButton          mScoreRemoveButton;
    private ImageButton          mLeftTeamTimeoutButton;
    private ImageButton          mRightTeamTimeoutButton;
    private LinearLayout         mLeftTeamTimeoutLayout;
    private LinearLayout         mRightTeamTimeoutLayout;
    private CountDown            mCountDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-GameActivity", "Create game activity");
        setContentView(R.layout.activity_game);

        if (!ServicesProvider.getInstance().areServicesAvailable()) {
            ServicesProvider.getInstance().restoreGameService(getApplicationContext());
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean keepScreenOnSetting = sharedPreferences.getBoolean("pref_keep_screen_on", false);
        if (keepScreenOnSetting) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (savedInstanceState == null) {
            setTitle("");
        }
        else {
            long duration = savedInstanceState.getLong("saved_timeout_duration");
            startToolbarCountDown(duration);
        }

        mGameService = ServicesProvider.getInstance().getGameService();
        mRecordedGamesService = ServicesProvider.getInstance().getRecordedGamesService();

        mGameService.addScoreListener(this);
        mGameService.addTimeoutListener(this);
        mGameService.addTeamListener(this);
        mRecordedGamesService.connectGameRecorder();

        mRandom = new Random();

        mLeftTeamNameText = findViewById(R.id.left_team_name_text);
        mRightTeamNameText = findViewById(R.id.right_team_name_text);

        mSwapTeamsButton = findViewById(R.id.swap_teams_button);

        mLeftTeamScoreButton = findViewById(R.id.left_team_score_button);
        mRightTeamScoreButton = findViewById(R.id.right_team_score_button);

        mLeftTeamSetsText = findViewById(R.id.left_team_set_text);
        mRightTeamSetsText = findViewById(R.id.right_team_set_text);

        mLeftTeamServiceButton = findViewById(R.id.left_team_service_button);
        mRightTeamServiceButton = findViewById(R.id.right_team_service_button);

        mSetsText = findViewById(R.id.set_text);

        mScoreRemoveButton = findViewById(R.id.score_remove_button);

        mLeftTeamTimeoutButton = findViewById(R.id.left_team_timeout_button);
        mRightTeamTimeoutButton = findViewById(R.id.right_team_timeout_button);

        mLeftTeamTimeoutLayout = findViewById(R.id.left_team_timeout_layout);
        mRightTeamTimeoutLayout = findViewById(R.id.right_team_timeout_layout);

        if (mGameService.getRules().areTeamTimeoutsEnabled()) {
            for (int index = 0; index < mGameService.getRules().getTeamTimeoutsPerSet(); index++) {
                final ImageView leftTimeout = new ImageView(this);
                leftTimeout.setImageResource(R.drawable.timeout_shape);
                final LinearLayout.LayoutParams leftTimeoutLayout = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                int margin = (int) getResources().getDimension(R.dimen.tiny_margin_size);
                leftTimeoutLayout.topMargin = margin;
                leftTimeoutLayout.bottomMargin = margin;
                leftTimeoutLayout.leftMargin = margin;
                leftTimeoutLayout.rightMargin = margin;
                leftTimeout.setLayoutParams(leftTimeoutLayout);
                mLeftTeamTimeoutLayout.addView(leftTimeout);

                final ImageView rightTimeout = new ImageView(this);
                rightTimeout.setImageResource(R.drawable.timeout_shape);
                final LinearLayout.LayoutParams rightTimeoutLayout = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                margin = (int) getResources().getDimension(R.dimen.tiny_margin_size);
                rightTimeoutLayout.topMargin = margin;
                rightTimeoutLayout.bottomMargin = margin;
                rightTimeoutLayout.leftMargin = margin;
                rightTimeoutLayout.rightMargin = margin;
                rightTimeout.setLayoutParams(rightTimeoutLayout);
                mRightTeamTimeoutLayout.addView(rightTimeout);
            }
        } else {
            mLeftTeamTimeoutButton.setVisibility(View.INVISIBLE);
            mRightTeamTimeoutButton.setVisibility(View.INVISIBLE);

            mLeftTeamTimeoutLayout.setVisibility(View.GONE);
            mRightTeamTimeoutLayout.setVisibility(View.GONE);
        }

        final ViewPager gamePager = findViewById(R.id.game_pager);
        final GameFragmentPagerAdapter gamePagerAdapter = new GameFragmentPagerAdapter(this, getSupportFragmentManager());
        gamePager.setAdapter(gamePagerAdapter);

        final TabLayout gameTabs = findViewById(R.id.game_tabs);
        gameTabs.setupWithViewPager(gamePager);

        if (gamePagerAdapter.getCount() == 1) {
            gameTabs.setVisibility(View.GONE);
        }

        mTeamOnLeftSide = mGameService.getTeamOnLeftSide();
        mTeamOnRightSide = mGameService.getTeamOnRightSide();
        onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, ActionOriginType.USER);

        if (mGameService.isMatchCompleted()) {
            disableView();
        }

        Animation courtAnimation = AnimationUtils.loadAnimation(this, R.anim.translate_from_right);
        gamePager.startAnimation(courtAnimation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGameService.removeScoreListener(this);
        mGameService.removeTimeoutListener(this);
        mGameService.removeTeamListener(this);
        mRecordedGamesService.disconnectGameRecorder(isFinishing());
        deleteToolbarCountdown();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCountDown != null) {
            outState.putLong("saved_timeout_duration", mCountDown.getDuration());
            deleteToolbarCountdown();
        }
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
        Log.i("VBR-GameActivity", "Toss a coin");
        final String tossResult = mRandom.nextBoolean() ? getResources().getString(R.string.toss_heads) : getResources().getString(R.string.toss_tails);
        Toast.makeText(this, tossResult, Toast.LENGTH_LONG).show();
    }

    private void navigateToHomeWithDialog() {
        Log.i("VBR-GameActivity", "Navigate to home");
        if (mGameService.isMatchCompleted()) {
            navigateToHome();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
            builder.setTitle(getResources().getString(R.string.navigate_home)).setMessage(getResources().getString(R.string.navigate_home_question));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    navigateToHome();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });

            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        }
    }

    private void navigateToHome() {
        UiUtils.navigateToHome(this, false);
    }

    private void share() {
        Log.i("VBR-GameActivity", "Share game");
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
        Log.i("VBR-GameActivity", "Swap teams");
        UiUtils.animate(this, mSwapTeamsButton);
        mGameService.swapTeams(ActionOriginType.USER);
    }

    public void swapFirstService(View view) {
        Log.i("VBR-GameActivity", "Swap first service");
        mGameService.swapServiceAtStart();
    }

    public void removeLastPoint(View view) {
        Log.i("VBR-GameActivity", "Remove last point");
        UiUtils.animate(this, mScoreRemoveButton);
        mGameService.removeLastPoint();
    }

    public void increaseLeftScore(View view) {
        Log.i("VBR-GameActivity", "Increase left score");
        UiUtils.animate(this, mLeftTeamScoreButton);
        increaseScoreWithDialog(mTeamOnLeftSide);
    }

    public void callLeftTimeout(View view) {
        Log.i("VBR-GameActivity", "Call left timeout");
        UiUtils.animate(this, mLeftTeamTimeoutButton);
        callTimeoutWithDialog(mTeamOnLeftSide);
    }

    public void increaseRightScore(View view) {
        Log.i("VBR-GameActivity", "Increase right score");
        UiUtils.animate(this, mRightTeamScoreButton);
        increaseScoreWithDialog(mTeamOnRightSide);
    }

    public void callRightTimeout(View view) {
        Log.i("VBR-GameActivity", "Call right timeout");
        UiUtils.animate(this, mRightTeamTimeoutButton);
        callTimeoutWithDialog(mTeamOnRightSide);
    }

    private void increaseScoreWithDialog(final TeamType teamType) {
        if ((mGameService.isMatchPoint() || mGameService.isSetPoint()) && mGameService.getLeadingTeam().equals(teamType)) {
            String title = mGameService.isMatchPoint() ? getResources().getString(R.string.match_point) : getResources().getString(R.string.set_point);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
            builder.setTitle(title).setMessage(getResources().getString(R.string.confirm_set_question));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("VBR-GameActivity", "User accepts the set point");
                    mGameService.addPoint(teamType);
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("VBR-GameActivity", "User refuses the set point");
                }
            });
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        } else {
            mGameService.addPoint(teamType);
        }
    }

    private void callTimeoutWithDialog(final TeamType teamType) {
        if (mGameService.getRemainingTimeouts(teamType) > 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
            builder.setTitle(String.format(getResources().getString(R.string.timeout_title), mGameService.getTeamName(teamType))).setMessage(getResources().getString(R.string.timeout_question));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("VBR-GameActivity", "User accepts the timeout");
                    mGameService.callTimeout(teamType);
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("VBR-GameActivity", "User refuses the timeout");
                }
            });
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        } else {
            Toast.makeText(this, String.format(getResources().getString(R.string.all_timeouts_called), mGameService.getTeamName(teamType)), Toast.LENGTH_LONG).show();
        }
    }

    // Listeners

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mTeamOnLeftSide = leftTeamType;
        mTeamOnRightSide = rightTeamType;

        // Left
        mLeftTeamNameText.setText(mGameService.getTeamName(mTeamOnLeftSide));
        UiUtils.colorTeamButton(this, mGameService.getTeamColor(mTeamOnLeftSide), mLeftTeamScoreButton);
        UiUtils.colorTeamIconButton(this, mGameService.getTeamColor(mTeamOnLeftSide), mLeftTeamTimeoutButton);

        // Right

        mRightTeamNameText.setText(mGameService.getTeamName(mTeamOnRightSide));
        UiUtils.colorTeamButton(this, mGameService.getTeamColor(mTeamOnRightSide), mRightTeamScoreButton);
        UiUtils.colorTeamIconButton(this, mGameService.getTeamColor(mTeamOnRightSide), mRightTeamTimeoutButton);

        onPointsUpdated(mTeamOnLeftSide, mGameService.getPoints(mTeamOnLeftSide));
        onSetsUpdated(mTeamOnLeftSide, mGameService.getSets(mTeamOnLeftSide));
        onTimeoutUpdated(mTeamOnLeftSide, mGameService.getRules().getTeamTimeoutsPerSet(), mGameService.getRemainingTimeouts(mTeamOnLeftSide));

        onPointsUpdated(mTeamOnRightSide, mGameService.getPoints(mTeamOnRightSide));
        onSetsUpdated(mTeamOnRightSide, mGameService.getSets(mTeamOnRightSide));
        onTimeoutUpdated(mTeamOnRightSide, mGameService.getRules().getTeamTimeoutsPerSet(), mGameService.getRemainingTimeouts(mTeamOnRightSide));

        onServiceSwapped(mGameService.getServingTeam());

        UiUtils.animateBounce(this, mLeftTeamNameText);
        UiUtils.animateBounce(this, mRightTeamNameText);
        UiUtils.animateBounce(this, mLeftTeamScoreButton);
        UiUtils.animateBounce(this, mRightTeamScoreButton);
        UiUtils.animateBounce(this, mLeftTeamSetsText);
        UiUtils.animateBounce(this, mRightTeamSetsText);
        UiUtils.animateBounce(this, mSetsText);
        UiUtils.animateBounce(this, mLeftTeamTimeoutButton);
        UiUtils.animateBounce(this, mRightTeamTimeoutButton);

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

        StringBuilder builder = new StringBuilder();
        builder.append("\t\t");
        for (int index = 0; index < mGameService.getNumberOfSets(); index++) {
            int leftPointsCount = mGameService.getPoints(mTeamOnLeftSide, index);
            int rightPointsCount = mGameService.getPoints(mTeamOnRightSide, index);
            builder.append(String.valueOf(leftPointsCount)).append('-').append(String.valueOf(rightPointsCount)).append("\t\t");
        }
        mSetsText.setText(builder.toString());

        if (mGameService.isMatchPoint()) {
            String text = getResources().getString(R.string.match_point);
            setTitle(text);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        } else if (mGameService.isSetPoint()) {
            String text = getResources().getString(R.string.set_point);
            setTitle(text);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        } else {
            setTitle("");
        }
    }

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {
        if (mTeamOnLeftSide.equals(teamType)) {
            mLeftTeamSetsText.setText(String.valueOf(newCount));
        } else {
            mRightTeamSetsText.setText(String.valueOf(newCount));
        }
    }

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
        disableView();
        invalidateOptionsMenu();
        Toast.makeText(this, String.format(getResources().getString(R.string.won_game), mGameService.getTeamName(winner)), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount) {
        final LinearLayout linearLayout;

        if (mTeamOnLeftSide.equals(teamType)) {
            linearLayout = mLeftTeamTimeoutLayout;
        } else {
            linearLayout = mRightTeamTimeoutLayout;
        }

        for (int index = 0; index < linearLayout.getChildCount(); index++) {
            View view = linearLayout.getChildAt(index);

            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;

                final int colorId;

                if (maxCount - newCount > index) {
                    colorId = R.color.colorButtonDisabled;
                } else {
                    colorId = android.R.color.holo_orange_dark;
                }

                imageView.setColorFilter(ContextCompat.getColor(this, colorId), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    @Override
    public void onTimeout(TeamType teamType, int duration) {
        deleteToolbarCountdown();
        CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, String.format(getResources().getString(R.string.timeout_title), mGameService.getTeamName(teamType)));
        timeoutFragment.show(getFragmentManager(), "timeout");

        if (mGameService.getRemainingTimeouts(teamType) == 0) {
            Toast.makeText(this, String.format(getResources().getString(R.string.all_timeouts_called), mGameService.getTeamName(teamType)), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTechnicalTimeout(int duration) {
        deleteToolbarCountdown();
        CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, getResources().getString(R.string.technical_timeout_title));
        timeoutFragment.show(getFragmentManager(), "technical_timeout");
    }

    @Override
    public void onGameInterval(int duration) {
        deleteToolbarCountdown();
        CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, getResources().getString(R.string.game_interval_title));
        timeoutFragment.show(getFragmentManager(), "game_interval");
    }

    public void startToolbarCountDown(long duration) {
        if (duration > 0L) {
            deleteToolbarCountdown();
            mCountDown = new CountDown(duration);
            mCountDown.setCountDownTimer(new CountDownTimer(mCountDown.getDuration(), 1000L) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (mCountDown != null) {
                        mCountDown.setDuration(millisUntilFinished);
                        setTitle(mCountDown.format(millisUntilFinished));
                    }
                }

                @Override
                public void onFinish() {
                    mCountDown = null;
                    UiUtils.playNotificationSound(GameActivity.this);
                    // Reset the title and possibly replaces it with set point or match point
                    setTitle("");
                    onPointsUpdated(TeamType.HOME, mGameService.getPoints(TeamType.HOME));
                }
            });

            mCountDown.getCountDownTimer().start();
        }
    }

    private void deleteToolbarCountdown() {
        if (mCountDown != null) {
            mCountDown.getCountDownTimer().cancel();
            mCountDown = null;
            setTitle("");
            onPointsUpdated(TeamType.HOME, mGameService.getPoints(TeamType.HOME));
        }
    }

    private void disableView() {
        Log.i("VBR-GameActivity", "Disable game activity view");
        mSwapTeamsButton.setEnabled(false);
        mLeftTeamScoreButton.setEnabled(false);
        mRightTeamScoreButton.setEnabled(false);
        mScoreRemoveButton.setEnabled(false);
        mLeftTeamTimeoutButton.setEnabled(false);
        mRightTeamTimeoutButton.setEnabled(false);
    }

}
