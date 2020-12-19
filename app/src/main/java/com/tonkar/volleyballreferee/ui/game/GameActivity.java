package com.tonkar.volleyballreferee.ui.game;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.GeneralListener;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionListener;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.game.score.ScoreListener;
import com.tonkar.volleyballreferee.engine.game.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesManager;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesService;
import com.tonkar.volleyballreferee.engine.team.TeamListener;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.game.court.BeachCourtFragment;
import com.tonkar.volleyballreferee.ui.game.court.Indoor4x4CourtFragment;
import com.tonkar.volleyballreferee.ui.game.court.IndoorCourtFragment;
import com.tonkar.volleyballreferee.ui.game.court.SnowCourtFragment;
import com.tonkar.volleyballreferee.ui.game.ladder.LaddersFragment;
import com.tonkar.volleyballreferee.ui.game.sanction.SanctionSelectionDialogFragment;
import com.tonkar.volleyballreferee.ui.game.sanction.SanctionsFragment;
import com.tonkar.volleyballreferee.ui.game.substitution.SubstitutionsFragment;
import com.tonkar.volleyballreferee.ui.game.timeout.CountDown;
import com.tonkar.volleyballreferee.ui.game.timeout.CountDownDialogFragment;
import com.tonkar.volleyballreferee.ui.game.timeout.TimeoutsFragment;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.interfaces.StoredGamesServiceHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;

public class GameActivity extends AppCompatActivity implements GeneralListener, ScoreListener, TimeoutListener, TeamListener, SanctionListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private IGame                mGame;
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
    private FloatingActionButton mUndoGameEventButton;
    private FloatingActionButton mLeftTeamTimeoutButton;
    private FloatingActionButton mRightTeamTimeoutButton;
    private LinearLayout         mLeftTeamTimeoutLayout;
    private LinearLayout         mRightTeamTimeoutLayout;
    private FloatingActionButton mLeftTeamCardsButton;
    private FloatingActionButton mRightTeamCardsButton;
    private CountDown            mCountDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredGamesService = new StoredGamesManager(this);
        mGame = mStoredGamesService.loadCurrentGame();

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

        if (mGame == null || mStoredGamesService == null) {
            UiUtils.navigateToHome(this);
        } else {
            mGame.addGeneralListener(this);
            mGame.addScoreListener(this);
            mGame.addTimeoutListener(this);
            mGame.addTeamListener(this);
            mGame.addSanctionListener(this);
            mStoredGamesService.connectGameRecorder(mGame);

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

            mUndoGameEventButton = findViewById(R.id.undo_game_event_button);

            mLeftTeamTimeoutButton = findViewById(R.id.left_team_timeout_button);
            mRightTeamTimeoutButton = findViewById(R.id.right_team_timeout_button);

            mLeftTeamTimeoutLayout = findViewById(R.id.left_team_timeout_layout);
            mRightTeamTimeoutLayout = findViewById(R.id.right_team_timeout_layout);

            mLeftTeamCardsButton = findViewById(R.id.left_team_cards_button);
            mRightTeamCardsButton = findViewById(R.id.right_team_cards_button);

            if (mGame.getRules().isTeamTimeouts()) {
                for (int index = 0; index < mGame.getRules().getTeamTimeoutsPerSet(); index++) {
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

            if (!mGame.getRules().isSanctions() || !UsageType.NORMAL.equals(mGame.getUsage())) {
                mLeftTeamCardsButton.hide();
                mRightTeamCardsButton.hide();
            }

            final BottomNavigationView gameNavigation = findViewById(R.id.game_nav);
            initGameNavigation(gameNavigation, savedInstanceState);

            mTeamOnLeftSide = mGame.getTeamOnLeftSide();
            mTeamOnRightSide = mGame.getTeamOnRightSide();
            onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, ActionOriginType.USER);

            if (mGame.isMatchCompleted()) {
                disableView();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGame != null) {
            mGame.removeGeneralListener(this);
            mGame.removeScoreListener(this);
            mGame.removeTimeoutListener(this);
            mGame.removeTeamListener(this);
            mGame.removeSanctionListener(this);

            if (mStoredGamesService != null) {
                mStoredGamesService.disconnectGameRecorder(isFinishing());
            }
        }

        deleteToolbarCountdown();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCountDown != null) {
            outState.putLong("saved_timeout_duration", mCountDown.getDuration());
            deleteToolbarCountdown();
        }
    }

    @Override
    public void onBackPressed() {
        UiUtils.navigateToHomeWithDialog(this, mGame);
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
        if (item.getItemId() == R.id.action_game_action_menu) {
            showGameActionMenu();
            return true;
        } else {
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
        mGame.swapTeams(ActionOriginType.USER);
    }

    public void swapFirstService(View view) {
        Log.i(Tags.GAME_UI, "Swap first service");
        mGame.swapServiceAtStart();
    }

    public void undoGameEvent(View view) {
        Log.i(Tags.GAME_UI, "Undo game event");
        UiUtils.animate(this, mUndoGameEventButton);
        showUndoDialog();
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
        if ((mGame.isMatchPoint() || mGame.isSetPoint()) && mGame.getLeadingTeam().equals(teamType)) {
            String title = mGame.isMatchPoint() ? getString(R.string.match_point) : getString(R.string.set_point);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
            builder.setTitle(title).setMessage(getString(R.string.confirm_set_question));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                Log.i(Tags.GAME_UI, "User accepts the set point");
                mGame.addPoint(teamType);
            });
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> Log.i(Tags.GAME_UI, "User rejects the set point"));
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        } else {
            mGame.addPoint(teamType);
        }
    }

    private void callTimeoutWithDialog(final TeamType teamType) {
        if (mGame.countRemainingTimeouts(teamType) > 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
            builder.setTitle(String.format(getString(R.string.timeout_title), mGame.getTeamName(teamType))).setMessage(getString(R.string.timeout_question));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                Log.i(Tags.GAME_UI, "User accepts the timeout");
                mGame.callTimeout(teamType);
            });
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> Log.i(Tags.GAME_UI, "User rejects the timeout"));
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        } else {
            UiUtils.showNotification(findViewById(R.id.activity_game_content), String.format(getString(R.string.all_timeouts_called), mGame.getTeamName(teamType)));
        }
    }

    private void showSanctionDialog(final TeamType teamType) {
        final String title = String.format(Locale.getDefault(), getString(R.string.sanction), mGame.getTeamName(teamType));
        SanctionSelectionDialogFragment sanctionSelectionDialogFragment = SanctionSelectionDialogFragment.newInstance(title, teamType);
        sanctionSelectionDialogFragment.show(getSupportFragmentManager(), "sanction_dialog");
    }

    private void showUndoDialog() {
        UndoDialogFragment undoDialogFragment = UndoDialogFragment.newInstance();
        undoDialogFragment.show(getSupportFragmentManager(), "undo_dialog");
    }

    // Listeners

    @Override
    public void onStartingLineupSubmitted(TeamType teamType) {}

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mTeamOnLeftSide = leftTeamType;
        mTeamOnRightSide = rightTeamType;

        // Left
        mLeftTeamNameText.setText(mGame.getTeamName(mTeamOnLeftSide));
        int leftColor = mGame.getTeamColor(mTeamOnLeftSide);
        UiUtils.colorTeamButton(this, leftColor, mLeftTeamScoreButton);
        UiUtils.colorTeamIconButton(this, leftColor, mLeftTeamTimeoutButton);
        UiUtils.colorTeamIconButton(this, leftColor, mLeftTeamCardsButton);

        // Right

        mRightTeamNameText.setText(mGame.getTeamName(mTeamOnRightSide));
        int rightColor = mGame.getTeamColor(mTeamOnRightSide);
        UiUtils.colorTeamButton(this, rightColor, mRightTeamScoreButton);
        UiUtils.colorTeamIconButton(this, rightColor, mRightTeamTimeoutButton);
        UiUtils.colorTeamIconButton(this, rightColor, mRightTeamCardsButton);

        onPointsUpdated(mTeamOnLeftSide, mGame.getPoints(mTeamOnLeftSide));
        onSetsUpdated(mTeamOnLeftSide, mGame.getSets(mTeamOnLeftSide));
        onTimeoutUpdated(mTeamOnLeftSide, mGame.getRules().getTeamTimeoutsPerSet(), mGame.countRemainingTimeouts(mTeamOnLeftSide));

        onPointsUpdated(mTeamOnRightSide, mGame.getPoints(mTeamOnRightSide));
        onSetsUpdated(mTeamOnRightSide, mGame.getSets(mTeamOnRightSide));
        onTimeoutUpdated(mTeamOnRightSide, mGame.getRules().getTeamTimeoutsPerSet(), mGame.countRemainingTimeouts(mTeamOnRightSide));

        onServiceSwapped(mGame.getServingTeam(), false);

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
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {}

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {}

    @Override
    public void onCanLetLiberoIn(TeamType defendingTeam, int number) {}

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        if (mTeamOnLeftSide.equals(teamType)) {
            mLeftTeamScoreButton.setText(UiUtils.formatNumberFromLocale(newCount));
        } else {
            mRightTeamScoreButton.setText(UiUtils.formatNumberFromLocale(newCount));
        }

        StringBuilder builder = new StringBuilder();
        builder.append("\t\t");
        for (int index = 0; index < mGame.getNumberOfSets(); index++) {
            int leftPointsCount = mGame.getPoints(mTeamOnLeftSide, index);
            int rightPointsCount = mGame.getPoints(mTeamOnRightSide, index);
            builder.append(UiUtils.formatScoreFromLocale(leftPointsCount, rightPointsCount, false)).append("\t\t");
        }
        mSetsText.setText(builder.toString().trim());

        if (mGame.isMatchPoint()) {
            String text = getString(R.string.match_point);
            setActionBarTitle(text);
            if (mGame.areNotificationsEnabled()) {
                UiUtils.makeText(this, text, Toast.LENGTH_LONG).show();
            }
        } else if (mGame.isSetPoint()) {
            String text = getString(R.string.set_point);
            setActionBarTitle(text);
            if (mGame.areNotificationsEnabled()) {
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
    public void onServiceSwapped(TeamType teamType, boolean isStart) {
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
        UiUtils.makeText(this, String.format(getString(R.string.won_game), mGame.getTeamName(winner)), Toast.LENGTH_LONG).show();
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
                    colorId = R.color.colorTimeout;
                }

                imageView.setColorFilter(ContextCompat.getColor(this, colorId), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    @Override
    public void onTimeout(TeamType teamType, int duration) {
        deleteToolbarCountdown();
        deleteFragmentCountdown();
        CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, String.format(getString(R.string.timeout_title), mGame.getTeamName(teamType)));
        timeoutFragment.show(getSupportFragmentManager(), "timeout");

        if (mGame.countRemainingTimeouts(teamType) == 0) {
            UiUtils.showNotification(findViewById(R.id.activity_game_content), String.format(getString(R.string.all_timeouts_called), mGame.getTeamName(teamType)));
        }
    }

    @Override
    public void onTechnicalTimeout(int duration) {
        if (mGame.areNotificationsEnabled()) {
            deleteToolbarCountdown();
            deleteFragmentCountdown();
            CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration, getString(R.string.technical_timeout_title));
            timeoutFragment.show(getSupportFragmentManager(), "timeout");
        }
    }

    @Override
    public void onGameInterval(int duration) {
        if (mGame.areNotificationsEnabled()) {
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
                    onPointsUpdated(TeamType.HOME, mGame.getPoints(TeamType.HOME));
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
            onPointsUpdated(TeamType.HOME, mGame.getPoints(TeamType.HOME));
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
        mUndoGameEventButton.setEnabled(false);
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
            case DELAY_WARNING:
                sanctionText = getString(R.string.yellow_card);
                break;
            case RED:
            case DELAY_PENALTY:
                sanctionText = getString(R.string.red_card);
                break;
            case RED_EXPULSION:
                sanctionText = getString(R.string.red_card_expulsion);
                break;
            case RED_DISQUALIFICATION:
                sanctionText = getString(R.string.red_card_disqualification);
                break;
        }

        UiUtils.makeText(this, sanctionText, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUndoSanction(TeamType teamType, SanctionType sanctionType, int number) {}

    private void initGameNavigation(final BottomNavigationView gameNavigation, Bundle savedInstanceState) {
        gameNavigation.setOnNavigationItemSelectedListener(item -> {
                    final Fragment fragment;

                    switch (item.getItemId()) {
                        case R.id.court_position_tab:
                            switch (mGame.getKind()) {
                                case INDOOR_4X4:
                                    fragment = Indoor4x4CourtFragment.newInstance();
                                    break;
                                case BEACH:
                                    fragment = BeachCourtFragment.newInstance();
                                    break;
                                case SNOW:
                                    fragment = SnowCourtFragment.newInstance();
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

        if (!mGame.getRules().isTeamTimeouts()) {
            gameNavigation.getMenu().removeItem(R.id.timeouts_tab);
        }

        if (!mGame.getRules().isSanctions()
                || UsageType.POINTS_SCOREBOARD.equals(mGame.getUsage())) {
            gameNavigation.getMenu().removeItem(R.id.sanctions_tab);
        }

        if (GameType.BEACH.equals(mGame.getKind())) {
            gameNavigation.getMenu().removeItem(R.id.substitutions_tab);
        }

        if (UsageType.NORMAL.equals(mGame.getUsage())) {
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
    public void onAttachFragment(@NonNull Fragment fragment) {
        if (fragment instanceof GameServiceHandler) {
            GameServiceHandler gameServiceHandler = (GameServiceHandler) fragment;
            gameServiceHandler.setGameService(mGame);
        }
        if (fragment instanceof StoredGamesServiceHandler) {
            StoredGamesServiceHandler storedGamesServiceHandler = (StoredGamesServiceHandler) fragment;
            storedGamesServiceHandler.setStoredGamesService(mStoredGamesService);
        }
    }

}
