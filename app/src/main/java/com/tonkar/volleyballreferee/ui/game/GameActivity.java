package com.tonkar.volleyballreferee.ui.game;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.StoredGames;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.GeneralListener;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionListener;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.data.StoredGamesService;
import com.tonkar.volleyballreferee.interfaces.score.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.interfaces.StoredGamesServiceHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;

public class GameActivity extends AppCompatActivity implements GeneralListener, ScoreListener, TimeoutListener, TeamListener, SanctionListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private GameService          mGameService;
    private StoredGamesService   mStoredGamesService;
    private TeamType             mTeamOnLeftSide;
    private TeamType             mTeamOnRightSide;
    private TextView             mLeftTeamNameText;
    private TextView             mRightTeamNameText;
    private FloatingActionButton mSwapTeamsButton;
    private MaterialButton       mLeftTeamScoreButton;
    private MaterialButton       mRightTeamScoreButton;
    private TextView             mLeftTeamSetsText;
    private TextView             mRightTeamSetsText;
    private FloatingActionButton mLeftTeamServiceButton;
    private FloatingActionButton mRightTeamServiceButton;
    private TextView             mSetsText;
    private FloatingActionButton mScoreRemoveButton;
    private FloatingActionButton mLeftTeamTimeoutButton;
    private FloatingActionButton mRightTeamTimeoutButton;
    private LinearLayout         mLeftTeamTimeoutLayout;
    private LinearLayout         mRightTeamTimeoutLayout;
    private FloatingActionButton mLeftTeamCardsButton;
    private FloatingActionButton mRightTeamCardsButton;
    private CountDown            mCountDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredGamesService = new StoredGames(this);
        mGameService = mStoredGamesService.loadCurrentGame();

        super.onCreate(savedInstanceState);

        Log.i(Tags.GAME_UI, "Create game activity");
        setContentView(R.layout.activity_game);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean keepScreenOnSetting = sharedPreferences.getBoolean(PrefUtils.PREF_KEEP_SCREEN_ON, false);
        if (keepScreenOnSetting) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            setActionBarTitle("");
        }
        else {
            long duration = savedInstanceState.getLong("saved_timeout_duration");
            startToolbarCountDown(duration);
        }

        if (mGameService == null || mStoredGamesService == null) {
            UiUtils.navigateToHome(this);
        } else {
            mGameService.addGeneralListener(this);
            mGameService.addScoreListener(this);
            mGameService.addTimeoutListener(this);
            mGameService.addTeamListener(this);
            mGameService.addSanctionListener(this);
            mStoredGamesService.connectGameRecorder(mGameService);

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

            mLeftTeamCardsButton = findViewById(R.id.left_team_cards_button);
            mRightTeamCardsButton = findViewById(R.id.right_team_cards_button);

            UiUtils.fixFabCompatPadding(mSwapTeamsButton);
            UiUtils.fixFabCompatPadding(mLeftTeamServiceButton);
            UiUtils.fixFabCompatPadding(mRightTeamServiceButton);
            UiUtils.fixFabCompatPadding(mScoreRemoveButton);
            UiUtils.fixFabCompatPadding(mLeftTeamTimeoutButton);
            UiUtils.fixFabCompatPadding(mLeftTeamCardsButton);
            UiUtils.fixFabCompatPadding(mRightTeamTimeoutButton);
            UiUtils.fixFabCompatPadding(mRightTeamCardsButton);

            if (mGameService.getRules().isTeamTimeouts()) {
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
                mLeftTeamTimeoutButton.hide();
                mRightTeamTimeoutButton.hide();

                mLeftTeamTimeoutLayout.setVisibility(View.GONE);
                mRightTeamTimeoutLayout.setVisibility(View.GONE);
            }

            if (!mGameService.getRules().isSanctions() || !UsageType.NORMAL.equals(mGameService.getUsage())) {
                mLeftTeamCardsButton.hide();
                mRightTeamCardsButton.hide();
            }

            final BottomNavigationView gameNavigation = findViewById(R.id.game_nav);
            initGameNavigation(gameNavigation, savedInstanceState);

            mTeamOnLeftSide = mGameService.getTeamOnLeftSide();
            mTeamOnRightSide = mGameService.getTeamOnRightSide();
            onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, ActionOriginType.USER);

            if (mGameService.isMatchCompleted()) {
                disableView();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGameService != null) {
            mGameService.removeGeneralListener(this);
            mGameService.removeScoreListener(this);
            mGameService.removeTimeoutListener(this);
            mGameService.removeTeamListener(this);
            mGameService.removeSanctionListener(this);

            if (mStoredGamesService != null) {
                mStoredGamesService.disconnectGameRecorder(isFinishing());
            }
        }

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
        UiUtils.navigateToHomeWithDialog(this, mGameService);
    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_game_action_menu:
                showGameActionMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showGameActionMenu() {
        GameActionMenu gameActionMenu = GameActionMenu.newInstance();
        gameActionMenu.show(getSupportFragmentManager(), "game_action_menu");
    }

    // UI Callbacks

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
        mGameService.removeLastPoint();
    }

    public void increaseLeftScore(View view) {
        Log.i(Tags.GAME_UI, "Increase left score");
        UiUtils.animate(this, mLeftTeamScoreButton);
        increaseScoreWithDialog(mTeamOnLeftSide);
    }

    public void callLeftTimeout(View view) {
        Log.i(Tags.GAME_UI, "Call left timeout");
        UiUtils.animate(this, mLeftTeamTimeoutButton);
        callTimeoutWithDialog(mTeamOnLeftSide);
    }

    public void giveLeftSanction(View view) {
        Log.i(Tags.GAME_UI, "Give left sanction");
        UiUtils.animate(this, mLeftTeamCardsButton);
        showSanctionDialog(mTeamOnLeftSide);
    }

    public void increaseRightScore(View view) {
        Log.i(Tags.GAME_UI, "Increase right score");
        UiUtils.animate(this, mRightTeamScoreButton);
        increaseScoreWithDialog(mTeamOnRightSide);
    }

    public void callRightTimeout(View view) {
        Log.i(Tags.GAME_UI, "Call right timeout");
        UiUtils.animate(this, mRightTeamTimeoutButton);
        callTimeoutWithDialog(mTeamOnRightSide);
    }

    public void giveRightSanction(View view) {
        Log.i(Tags.GAME_UI, "Give right sanction");
        UiUtils.animate(this, mRightTeamCardsButton);
        showSanctionDialog(mTeamOnRightSide);
    }

    private void increaseScoreWithDialog(final TeamType teamType) {
        if ((mGameService.isMatchPoint() || mGameService.isSetPoint()) && mGameService.getLeadingTeam().equals(teamType)) {
            String title = mGameService.isMatchPoint() ? getString(R.string.match_point) : getString(R.string.set_point);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
            builder.setTitle(title).setMessage(getString(R.string.confirm_set_question));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                Log.i(Tags.GAME_UI, "User accepts the set point");
                mGameService.addPoint(teamType);
            });
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> Log.i(Tags.GAME_UI, "User refuses the set point"));
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        } else {
            mGameService.addPoint(teamType);
        }
    }

    private void callTimeoutWithDialog(final TeamType teamType) {
        if (mGameService.getRemainingTimeouts(teamType) > 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
            builder.setTitle(String.format(getString(R.string.timeout_title), mGameService.getTeamName(teamType))).setMessage(getString(R.string.timeout_question));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                Log.i(Tags.GAME_UI, "User accepts the timeout");
                mGameService.callTimeout(teamType);
            });
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> Log.i(Tags.GAME_UI, "User refuses the timeout"));
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        } else {
            UiUtils.showNotification(this, String.format(getString(R.string.all_timeouts_called), mGameService.getTeamName(teamType)));
        }
    }

    private void showSanctionDialog(final TeamType teamType) {
        final String title = String.format(Locale.getDefault(), getString(R.string.sanction), mGameService.getTeamName(teamType));
        SanctionSelectionDialog sanctionSelectionDialog = new SanctionSelectionDialog(getLayoutInflater(), this, title, mGameService, teamType) {
            @Override
            public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
                mGameService.giveSanction(teamType, sanctionType, number);
            }
        };
        sanctionSelectionDialog.show();
    }

    // Listeners

    @Override
    public void onStartingLineupSubmitted() {}

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mTeamOnLeftSide = leftTeamType;
        mTeamOnRightSide = rightTeamType;

        // Left
        mLeftTeamNameText.setText(mGameService.getTeamName(mTeamOnLeftSide));
        UiUtils.colorTeamButton(this, mGameService.getTeamColor(mTeamOnLeftSide), mLeftTeamScoreButton);
        UiUtils.colorTeamIconButton(this, mGameService.getTeamColor(mTeamOnLeftSide), mLeftTeamTimeoutButton);
        UiUtils.colorTeamIconButton(this, mGameService.getTeamColor(mTeamOnLeftSide), mLeftTeamCardsButton);

        // Right

        mRightTeamNameText.setText(mGameService.getTeamName(mTeamOnRightSide));
        UiUtils.colorTeamButton(this, mGameService.getTeamColor(mTeamOnRightSide), mRightTeamScoreButton);
        UiUtils.colorTeamIconButton(this, mGameService.getTeamColor(mTeamOnRightSide), mRightTeamTimeoutButton);
        UiUtils.colorTeamIconButton(this, mGameService.getTeamColor(mTeamOnRightSide), mRightTeamCardsButton);

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
        UiUtils.animateBounce(this, mLeftTeamCardsButton);
        UiUtils.animateBounce(this, mRightTeamCardsButton);

        if (mGameService.areNotificationsEnabled() && ActionOriginType.APPLICATION.equals(actionOriginType)) {
            if (GameType.BEACH.equals(mGameService.getKind())) {
                UiUtils.showNotification(this, getString(R.string.switch_sides));
                UiUtils.playNotificationSound(this);
            } else {
                UiUtils.makeText(this, getString(R.string.switch_sides), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {}

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {}

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        if (mTeamOnLeftSide.equals(teamType)) {
            mLeftTeamScoreButton.setText(UiUtils.formatNumberFromLocale(newCount));
        } else {
            mRightTeamScoreButton.setText(UiUtils.formatNumberFromLocale(newCount));
        }

        StringBuilder builder = new StringBuilder();
        builder.append("\t\t");
        for (int index = 0; index < mGameService.getNumberOfSets(); index++) {
            int leftPointsCount = mGameService.getPoints(mTeamOnLeftSide, index);
            int rightPointsCount = mGameService.getPoints(mTeamOnRightSide, index);
            builder.append(UiUtils.formatScoreFromLocale(leftPointsCount, rightPointsCount, false)).append("\t\t");
        }
        mSetsText.setText(builder.toString().trim());

        if (mGameService.isMatchPoint()) {
            String text = getString(R.string.match_point);
            setActionBarTitle(text);
            if (mGameService.areNotificationsEnabled()) {
                UiUtils.makeText(this, text, Toast.LENGTH_LONG).show();
            }
        } else if (mGameService.isSetPoint()) {
            String text = getString(R.string.set_point);
            setActionBarTitle(text);
            if (mGameService.areNotificationsEnabled()) {
                UiUtils.makeText(this, text, Toast.LENGTH_LONG).show();
            }
        } else {
            setActionBarTitle("");
        }
    }

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {
        if (mTeamOnLeftSide.equals(teamType)) {
            mLeftTeamSetsText.setText(UiUtils.formatNumberFromLocale(newCount));
        } else {
            mRightTeamSetsText.setText(UiUtils.formatNumberFromLocale(newCount));
        }
    }

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
        disableView();
        deleteToolbarCountdown();
        deleteFragmentCountdown();
        UiUtils.makeText(this, String.format(getString(R.string.won_game), mGameService.getTeamName(winner)), Toast.LENGTH_LONG).show();
        UiUtils.navigateToStoredGameAfterDelay(this, 5000L);
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
                    colorId = R.color.colorDisabledButton;
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
        deleteFragmentCountdown();
        CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, String.format(getString(R.string.timeout_title), mGameService.getTeamName(teamType)));
        timeoutFragment.show(getSupportFragmentManager(), "timeout");

        if (mGameService.getRemainingTimeouts(teamType) == 0) {
            UiUtils.showNotification(this, String.format(getString(R.string.all_timeouts_called), mGameService.getTeamName(teamType)));
        }
    }

    @Override
    public void onTechnicalTimeout(int duration) {
        if (mGameService.areNotificationsEnabled()) {
            deleteToolbarCountdown();
            deleteFragmentCountdown();
            CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, getString(R.string.technical_timeout_title));
            timeoutFragment.show(getSupportFragmentManager(), "timeout");
        }
    }

    @Override
    public void onGameInterval(int duration) {
        if (mGameService.areNotificationsEnabled()) {
            deleteToolbarCountdown();
            deleteFragmentCountdown();
            CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, getString(R.string.game_interval_title));
            timeoutFragment.show(getSupportFragmentManager(), "timeout");
        }
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
                        setActionBarTitle(mCountDown.format(millisUntilFinished));
                    }
                }

                @Override
                public void onFinish() {
                    mCountDown = null;
                    UiUtils.playNotificationSound(GameActivity.this);
                    // Reset the title and possibly replaces it with set point or match point
                    setActionBarTitle("");
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
            setActionBarTitle("");
            onPointsUpdated(TeamType.HOME, mGameService.getPoints(TeamType.HOME));
        }
    }

    private void deleteFragmentCountdown() {
        Fragment timeoutFragment = getSupportFragmentManager().findFragmentByTag("timeout");
        if (timeoutFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(timeoutFragment).commit();
        }
    }

    private void disableView() {
        Log.i(Tags.GAME_UI, "Disable game activity view");
        mSwapTeamsButton.setEnabled(false);
        mLeftTeamScoreButton.setEnabled(false);
        mRightTeamScoreButton.setEnabled(false);
        mScoreRemoveButton.setEnabled(false);
        mLeftTeamTimeoutButton.setEnabled(false);
        mRightTeamTimeoutButton.setEnabled(false);
        mLeftTeamCardsButton.setEnabled(false);
        mRightTeamCardsButton.setEnabled(false);
    }

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        String sanctionText = "";

        switch (sanctionType) {
            case YELLOW:
                sanctionText = getString(R.string.yellow_card);
                break;
            case RED:
                sanctionText = getString(R.string.red_card);
                break;
            case RED_EXPULSION:
                sanctionText = getString(R.string.red_card_expulsion);
                break;
            case RED_DISQUALIFICATION:
                sanctionText = getString(R.string.red_card_disqualification);
                break;
            case DELAY_WARNING:
                sanctionText = getString(R.string.yellow_card);
                break;
            case DELAY_PENALTY:
                sanctionText = getString(R.string.red_card);
                break;
        }

        UiUtils.makeText(this, sanctionText, Toast.LENGTH_LONG).show();
    }

    private void initGameNavigation(final BottomNavigationView gameNavigation, Bundle savedInstanceState) {
        gameNavigation.setOnNavigationItemSelectedListener(item -> {
                    final Fragment fragment;

                    switch (item.getItemId()) {
                        case R.id.court_position_tab:
                            switch (mGameService.getKind()) {
                                case INDOOR_4X4:
                                    fragment = Indoor4x4CourtFragment.newInstance();
                                    break;
                                case BEACH:
                                    fragment = BeachCourtFragment.newInstance();
                                    break;
                                case INDOOR:
                                default:
                                    fragment = IndoorCourtFragment.newInstance();
                                    break;
                            }
                            break;
                        case R.id.substitutions_tab:
                            fragment = SubstitutionsFragment.newInstance();
                            break;
                        case R.id.timeouts_tab:
                            fragment = TimeoutsFragment.newInstance();
                            break;
                        case R.id.sanctions_tab:
                            fragment = SanctionsFragment.newInstance();
                            break;
                        case R.id.ladder_tab:
                        default:
                            fragment = LaddersFragment.newInstance();
                            break;
                    }

                    final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    UiUtils.animateNavigationView(transaction);
                    transaction.replace(R.id.game_container, fragment).commit();

                    return true;
                }
        );

        if (!mGameService.getRules().isTeamTimeouts()) {
            gameNavigation.getMenu().removeItem(R.id.timeouts_tab);
        }

        if (!mGameService.getRules().isSanctions()
                || UsageType.POINTS_SCOREBOARD.equals(mGameService.getUsage())) {
            gameNavigation.getMenu().removeItem(R.id.sanctions_tab);
        }

        if (GameType.BEACH.equals(mGameService.getKind())) {
            gameNavigation.getMenu().removeItem(R.id.substitutions_tab);
        }

        if (UsageType.NORMAL.equals(mGameService.getUsage())) {
            if (savedInstanceState == null) {
                gameNavigation.setSelectedItemId(R.id.court_position_tab);
            }
        } else {
            gameNavigation.getMenu().removeItem(R.id.substitutions_tab);
            gameNavigation.getMenu().removeItem(R.id.court_position_tab);

            if (savedInstanceState == null) {
                gameNavigation.setSelectedItemId(R.id.ladder_tab);
            }
        }
    }

    @Override
    public void onMatchIndexed(boolean indexed) {
        if (indexed) {
            UiUtils.makeText(this, getString(R.string.public_game_message), Toast.LENGTH_LONG).show();
        } else {
            UiUtils.makeText(this, getString(R.string.private_game_message), Toast.LENGTH_LONG).show();
        }
    }

    private void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof GameServiceHandler) {
            GameServiceHandler gameServiceHandler = (GameServiceHandler) fragment;
            gameServiceHandler.setGameService(mGameService);
        }
        if (fragment instanceof StoredGamesServiceHandler) {
            StoredGamesServiceHandler storedGamesServiceHandler = (StoredGamesServiceHandler) fragment;
            storedGamesServiceHandler.setStoredGamesService(mStoredGamesService);
        }
    }

}
