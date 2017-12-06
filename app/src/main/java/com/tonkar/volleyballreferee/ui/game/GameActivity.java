package com.tonkar.volleyballreferee.ui.game;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.ScoreClient;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryClient;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.ScoreService;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeoutClient;
import com.tonkar.volleyballreferee.interfaces.TimeoutListener;
import com.tonkar.volleyballreferee.interfaces.TimeoutService;
import com.tonkar.volleyballreferee.ui.MainActivity;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.Random;

public class GameActivity extends AppCompatActivity implements ScoreClient, TimeoutClient, TeamClient, GamesHistoryClient, ScoreListener, TimeoutListener, TeamListener {

    private ScoreService        mScoreService;
    private TimeoutService      mTimeoutService;
    private TeamService         mTeamService;
    private GamesHistoryService mGamesHistoryService;
    private Random              mRandom;
    private TeamType            mTeamOnLeftSide;
    private TeamType            mTeamOnRightSide;
    private TextView            mLeftTeamNameText;
    private TextView            mRightTeamNameText;
    private ImageButton         mSwapTeamsButton;
    private Button              mLeftTeamScoreButton;
    private Button              mRightTeamScoreButton;
    private TextView            mLeftTeamSetsText;
    private TextView            mRightTeamSetsText;
    private ImageButton         mLeftTeamServiceButton;
    private ImageButton         mRightTeamServiceButton;
    private TextView            mSetsText;
    private ImageButton         mScoreRemoveButton;
    private ImageButton         mLeftTeamTimeoutButton;
    private ImageButton         mRightTeamTimeoutButton;
    private LinearLayout        mLeftTeamTimeoutLayout;
    private LinearLayout        mRightTeamTimeoutLayout;
    private Menu                mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Log.i("VBR-GameActivity", "Create game activity");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean keepScreenOnSetting = sharedPreferences.getBoolean("pref_keep_screen_on", false);
        if (keepScreenOnSetting) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setTitle("");

        setScoreService(ServicesProvider.getInstance().getScoreService());
        setTimeoutService(ServicesProvider.getInstance().getTimeoutService());
        setTeamService(ServicesProvider.getInstance().getTeamService());
        setGamesHistoryService(ServicesProvider.getInstance().getGameHistoryService());

        mScoreService.addScoreListener(this);
        mTimeoutService.addTimeoutListener(this);
        mTeamService.addTeamListener(this);
        mGamesHistoryService.connectGameRecorder();

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

        if (mScoreService.getRules().areTeamTimeoutsEnabled()) {
            for (int index = 0; index < mScoreService.getRules().getTeamTimeoutsPerSet(); index++) {
                final ImageView leftTimeout = new ImageView(this);
                leftTimeout.setImageResource(R.drawable.timeout_shape);
                final LinearLayout.LayoutParams leftTimeoutLayout = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                leftTimeout.setLayoutParams(leftTimeoutLayout);
                mLeftTeamTimeoutLayout.addView(leftTimeout);

                final ImageView rightTimeout = new ImageView(this);
                rightTimeout.setImageResource(R.drawable.timeout_shape);
                final LinearLayout.LayoutParams rightTimeoutLayout = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                rightTimeout.setLayoutParams(rightTimeoutLayout);
                mRightTeamTimeoutLayout.addView(rightTimeout);
            }
        }
        else {
            mLeftTeamTimeoutButton.setVisibility(View.INVISIBLE);
            mRightTeamTimeoutButton.setVisibility(View.INVISIBLE);

            mLeftTeamTimeoutLayout.setVisibility(View.GONE);
            mRightTeamTimeoutLayout.setVisibility(View.GONE);
        }

        final ViewPager gamePager = findViewById(R.id.game_pager);
        final GameFragmentPagerAdapter gamePagerAdapter = new GameFragmentPagerAdapter(mScoreService, this, getSupportFragmentManager());
        gamePager.setAdapter(gamePagerAdapter);

        final TabLayout gameTabs = findViewById(R.id.game_tabs);
        gameTabs.setupWithViewPager(gamePager);

        if (gamePagerAdapter.getCount() == 1) {
            gameTabs.setVisibility(View.GONE);
        }

        mTeamOnLeftSide = mTeamService.getTeamOnLeftSide();
        mTeamOnRightSide = mTeamService.getTeamOnRightSide();
        onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, ActionOriginType.USER);

        if (mScoreService.isMatchCompleted()) {
            disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScoreService.removeScoreListener(this);
        mTimeoutService.removeTimeoutListener(this);
        mTeamService.removeTeamListener(this);
        mGamesHistoryService.disconnectGameRecorder();
    }

    @Override
    public void onBackPressed() {
        navigateHomeWithDialog();
    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game, menu);
        mMenu = menu;

        if (mScoreService.isMatchCompleted()) {
            disableMenu();
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
                navigateHomeWithDialog();
                return true;
            case R.id.action_share:
                share();
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

    private void navigateHomeWithDialog() {
        Log.i("VBR-GameActivity", "Navigate home");
        if (mScoreService.isMatchCompleted()) {
            navigateHome();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
            builder.setTitle(getResources().getString(R.string.navigated_home)).setMessage(getResources().getString(R.string.navigated_home_question));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    navigateHome();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });

            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        }
    }

    private void navigateHome() {
        final Intent intent = new Intent(GameActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("auto_ignore_resume_game", true);
        startActivity(intent);
    }

    private void share() {
        Log.i("VBR-GameActivity", "Share game");
        UiUtils.shareScreen(this, getWindow(), mScoreService.getGameSummary());
    }

    // UI Callbacks

    public void swapTeams(View view) {
        Log.i("VBR-GameActivity", "Swap teams");
        mTeamService.swapTeams(ActionOriginType.USER);
    }

    public void swapFirstService(View view) {
        Log.i("VBR-GameActivity", "Swap first service");
        mScoreService.swapServiceAtStart();
    }

    public void removeLastPoint(View view) {
        Log.i("VBR-GameActivity", "Remove last point");
        mScoreService.removeLastPoint();
    }

    public void increaseLeftScore(View view) {
        Log.i("VBR-GameActivity", "Increase left score");
        increaseScoreWithDialog(mTeamOnLeftSide);
    }

    public void callLeftTimeout(View view) {
        Log.i("VBR-GameActivity", "Call left timeout");
        callTimeoutWithDialog(mTeamOnLeftSide);
    }

    public void increaseRightScore(View view) {
        Log.i("VBR-GameActivity", "Increase right score");
        increaseScoreWithDialog(mTeamOnRightSide);
    }

    private void increaseScoreWithDialog(final TeamType teamType) {
        if ((mScoreService.isMatchPoint() || mScoreService.isSetPoint()) && mScoreService.getLeadingTeam().equals(teamType)) {
            String title = mScoreService.isMatchPoint() ? getResources().getString(R.string.match_point) : getResources().getString(R.string.set_point);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
            builder.setTitle(title).setMessage(getResources().getString(R.string.confirm_set_question));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("VBR-GameActivity", "User accepts the set point");
                    mScoreService.addPoint(teamType);
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
            mScoreService.addPoint(teamType);
        }
    }

    public void callRightTimeout(View view) {
        Log.i("VBR-GameActivity", "Call right timeout");
        callTimeoutWithDialog(mTeamOnRightSide);
    }

    private void callTimeoutWithDialog(final TeamType teamType) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(String.format(getResources().getString(R.string.timeout_title), mTeamService.getTeamName(teamType))).setMessage(getResources().getString(R.string.timeout_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i("VBR-GameActivity", "User accepts the timeout");
                mTimeoutService.callTimeout(teamType);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i("VBR-GameActivity", "User refuses the timeout");
            }
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    // Listeners

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mTeamOnLeftSide = leftTeamType;
        mTeamOnRightSide = rightTeamType;

        // Left
        mLeftTeamNameText.setText(mTeamService.getTeamName(mTeamOnLeftSide));
        UiUtils.colorTeamButton(this, mTeamService.getTeamColor(mTeamOnLeftSide), mLeftTeamScoreButton);

        // Right

        mRightTeamNameText.setText(mTeamService.getTeamName(mTeamOnRightSide));
        UiUtils.colorTeamButton(this, mTeamService.getTeamColor(mTeamOnRightSide), mRightTeamScoreButton);

        onPointsUpdated(mTeamOnLeftSide, mScoreService.getPoints(mTeamOnLeftSide));
        onSetsUpdated(mTeamOnLeftSide, mScoreService.getSets(mTeamOnLeftSide));
        onTimeoutUpdated(mTeamOnLeftSide, mScoreService.getRules().getTeamTimeoutsPerSet(), mTimeoutService.getTimeouts(mTeamOnLeftSide));

        onPointsUpdated(mTeamOnRightSide, mScoreService.getPoints(mTeamOnRightSide));
        onSetsUpdated(mTeamOnRightSide, mScoreService.getSets(mTeamOnRightSide));
        onTimeoutUpdated(mTeamOnRightSide, mScoreService.getRules().getTeamTimeoutsPerSet(), mTimeoutService.getTimeouts(mTeamOnRightSide));

        onServiceSwapped(mScoreService.getServingTeam());

        if (ActionOriginType.APPLICATION.equals(actionOriginType)) {
            Toast.makeText(this, getResources().getString(R.string.switch_sides), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {}

    @Override
    public void onTeamRotated(TeamType teamType) {}

    @Override
    public void setScoreService(ScoreService scoreService) {
        mScoreService = scoreService;
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        if (mTeamOnLeftSide.equals(teamType)) {
            mLeftTeamScoreButton.setText(String.valueOf(newCount));
        } else {
            mRightTeamScoreButton.setText(String.valueOf(newCount));
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < mScoreService.getNumberOfSets(); index++) {
            int leftPointsCount = mScoreService.getPoints(mTeamOnLeftSide, index);
            int rightPointsCount = mScoreService.getPoints(mTeamOnRightSide, index);
            builder.append(String.valueOf(leftPointsCount)).append('-').append(String.valueOf(rightPointsCount)).append('\t');
        }
        mSetsText.setText(builder.toString());

        if (mScoreService.isMatchPoint()) {
            String text = getResources().getString(R.string.match_point);
            setTitle(text);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        } else if (mScoreService.isSetPoint()) {
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
    public void onSetCompleted() {}

    @Override
    public void onMatchCompleted(final TeamType winner) {
        disableAll();
        Toast.makeText(this, String.format(getResources().getString(R.string.won_game), mTeamService.getTeamName(winner)), Toast.LENGTH_LONG).show();
    }

    @Override
    public void setTimeoutService(TimeoutService timeoutService) {
        mTimeoutService = timeoutService;
    }

    @Override
    public void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount) {
        final LinearLayout linearLayout;
        final ImageButton imageButton;

        if (mTeamOnLeftSide.equals(teamType)) {
            linearLayout = mLeftTeamTimeoutLayout;
            imageButton = mLeftTeamTimeoutButton;
        } else {
            linearLayout = mRightTeamTimeoutLayout;
            imageButton = mRightTeamTimeoutButton;
        }

        imageButton.setEnabled(newCount != 0);

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
        CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, String.format(getResources().getString(R.string.timeout_title), mTeamService.getTeamName(teamType)));
        timeoutFragment.show(getFragmentManager(), "timeout");
    }

    @Override
    public void onTechnicalTimeout(int duration) {
        CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, getResources().getString(R.string.technical_timeout_title));
        timeoutFragment.show(getFragmentManager(), "technical_timeout");
    }

    @Override
    public void onGameInterval(int duration) {
        CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, getResources().getString(R.string.game_interval_title));
        timeoutFragment.show(getFragmentManager(), "game_interval");
    }

    @Override
    public void setGamesHistoryService(GamesHistoryService gamesHistoryService) {
        mGamesHistoryService = gamesHistoryService;
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

    private void disableMenu() {
        Log.i("VBR-GameActivity", "Disable game activity menu");
        final MenuItem tossCoinMenu = mMenu.findItem(R.id.action_toss_coin);
        tossCoinMenu.setEnabled(false);
    }

    private void disableAll() {
        Log.i("VBR-GameActivity", "Disable all game activity");
        disableView();
        disableMenu();
    }

}
