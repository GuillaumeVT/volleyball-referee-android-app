package com.tonkar.volleyballreferee.ui.game;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameClient;
import com.tonkar.volleyballreferee.interfaces.GameHistoryClient;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.GameListener;
import com.tonkar.volleyballreferee.interfaces.GameService;
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

public class GameActivity extends AppCompatActivity implements GameClient, TimeoutClient, TeamClient, GameHistoryClient, GameListener, TimeoutListener, TeamListener {

    private GameService         mGameService;
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

        setTitle("");

        setGameService(ServicesProvider.getInstance().getGameService());
        setTimeoutService(ServicesProvider.getInstance().getTimeoutService());
        setTeamService(ServicesProvider.getInstance().getTeamService());
        setGameHistoryService(ServicesProvider.getInstance().getGameHistoryService());

        mGameService.addGameListener(this);
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

        if (mGameService.getRules().areTeamTimeoutsEnabled()) {
            for (int index = 0; index < mGameService.getRules().getTeamTimeoutsPerSet(); index++) {
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
        gamePager.setAdapter(new GameFragmentPagerAdapter(mGameService, this, getSupportFragmentManager()));

        TabLayout gameTabs = findViewById(R.id.game_tabs);
        gameTabs.setupWithViewPager(gamePager);

        mTeamOnLeftSide = mTeamService.getTeamOnLeftSide();
        mTeamOnRightSide = mTeamService.getTeamOnRightSide();
        onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, ActionOriginType.USER);

        if (mGameService.isGameCompleted()) {
            disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGameService.removeGameListener(this);
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

        if (mGameService.isGameCompleted()) {
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
        if (mGameService.isGameCompleted()) {
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
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mGamesHistoryService.disableAutoSaveCurrentGame();
                    mGamesHistoryService.deleteCurrentGame();
                    navigateHome();
                }
            });
            builder.show();
        }
    }

    private void navigateHome() {
        final Intent intent = new Intent(GameActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void share() {
        Log.i("VBR-GameActivity", "Share game");
        UiUtils.shareScreen(this, getWindow(), mGameService.getGameSummary());
    }

    // UI Callbacks

    public void swapTeams(View view) {
        Log.i("VBR-GameActivity", "Swap teams");
        mTeamService.swapTeams(ActionOriginType.USER);
    }

    public void swapFirstService(View view) {
        Log.i("VBR-GameActivity", "Swap first service");
        mGameService.swapServiceAtStart();
    }

    public void removeLastPoint(View view) {
        Log.i("VBR-GameActivity", "Remove last point");
        mGameService.removeLastPoint();
    }

    public void increaseLeftScore(View view) {
        Log.i("VBR-GameActivity", "Increase left score");
        mGameService.addPoint(mTeamOnLeftSide);
    }

    public void callLeftTimeout(View view) {
        Log.i("VBR-GameActivity", "Call left timeout");
        callTimeout(mTeamOnLeftSide);
    }

    public void increaseRightScore(View view) {
        Log.i("VBR-GameActivity", "Increase right score");
        mGameService.addPoint(mTeamOnRightSide);
    }

    public void callRightTimeout(View view) {
        Log.i("VBR-GameActivity", "Call right timeout");
        callTimeout(mTeamOnRightSide);
    }

    private void callTimeout(final TeamType teamType) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.timeout)).setMessage(getResources().getString(R.string.timeout_question));
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
        builder.show();
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
        int leftBackgroundColor = ContextCompat.getColor(this, mTeamService.getTeamColor(mTeamOnLeftSide));
        mLeftTeamScoreButton.getBackground().setColorFilter(new PorterDuffColorFilter(leftBackgroundColor, PorterDuff.Mode.SRC));
        mLeftTeamScoreButton.setTextColor(UiUtils.getTextColor(this, leftBackgroundColor));

        // Right

        mRightTeamNameText.setText(mTeamService.getTeamName(mTeamOnRightSide));
        int rightBackgroundColor = ContextCompat.getColor(this, mTeamService.getTeamColor(mTeamOnRightSide));
        mRightTeamScoreButton.getBackground().setColorFilter(new PorterDuffColorFilter(rightBackgroundColor, PorterDuff.Mode.SRC));
        mRightTeamScoreButton.setTextColor(UiUtils.getTextColor(this, rightBackgroundColor));

        onPointsUpdated(mTeamOnLeftSide, mGameService.getPoints(mTeamOnLeftSide));
        onSetsUpdated(mTeamOnLeftSide, mGameService.getSets(mTeamOnLeftSide));
        onTimeoutUpdated(mTeamOnLeftSide, mGameService.getRules().getTeamTimeoutsPerSet(), mTimeoutService.getTimeouts(mTeamOnLeftSide));

        onPointsUpdated(mTeamOnRightSide, mGameService.getPoints(mTeamOnRightSide));
        onSetsUpdated(mTeamOnRightSide, mGameService.getSets(mTeamOnRightSide));
        onTimeoutUpdated(mTeamOnRightSide, mGameService.getRules().getTeamTimeoutsPerSet(), mTimeoutService.getTimeouts(mTeamOnRightSide));

        onServiceSwapped(mGameService.getServingTeam());

        if (ActionOriginType.APPLICATION.equals(actionOriginType)) {
            Toast.makeText(this, getResources().getString(R.string.switch_sides), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType) {}

    @Override
    public void onTeamRotated(TeamType teamType) {}

    @Override
    public void setGameService(GameService gameService) {
        mGameService = gameService;
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        if (mTeamOnLeftSide.equals(teamType)) {
            mLeftTeamScoreButton.setText(String.valueOf(newCount));
        } else {
            mRightTeamScoreButton.setText(String.valueOf(newCount));
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < mGameService.getNumberOfSets(); index++) {
            int leftPointsCount = mGameService.getPoints(mTeamOnLeftSide, index);
            int rightPointsCount = mGameService.getPoints(mTeamOnRightSide, index);
            builder.append(String.valueOf(leftPointsCount)).append('-').append(String.valueOf(rightPointsCount)).append('\t');
        }
        mSetsText.setText(builder.toString());

        if (mGameService.isSetPoint()) {
            setTitle(getResources().getString(R.string.set_point));
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
    public void onSetCompleted() {
        if (!mGameService.isGameCompleted() && mGameService.getRules().areGameIntervalsEnabled()) {
            CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(mGameService.getRules().getGameIntervalDuration(), getResources().getString(R.string.game_interval));
            timeoutFragment.show(getFragmentManager(), "gameinterval");
        }
    }

    @Override
    public void onGameCompleted(final TeamType winner) {
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
    public void onTimeout(int duration) {
        CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, getResources().getString(R.string.timeout));
        timeoutFragment.show(getFragmentManager(), "timeout");
    }

    @Override
    public void setGameHistoryService(GamesHistoryService gamesHistoryService) {
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
