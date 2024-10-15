package com.tonkar.volleyballreferee.ui.game;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.game.sanction.*;
import com.tonkar.volleyballreferee.engine.game.score.ScoreListener;
import com.tonkar.volleyballreferee.engine.game.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.game.court.*;
import com.tonkar.volleyballreferee.ui.game.ladder.LaddersFragment;
import com.tonkar.volleyballreferee.ui.game.sanction.*;
import com.tonkar.volleyballreferee.ui.game.substitution.SubstitutionsFragment;
import com.tonkar.volleyballreferee.ui.game.timeout.*;
import com.tonkar.volleyballreferee.ui.interfaces.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;

public class GameActivity extends AppCompatActivity implements ScoreListener, TimeoutListener, TeamListener, SanctionListener {

    private IGame              mGame;
    private StoredGamesService mStoredGamesService;
    private TeamType           mTeamOnLeftSide;
    private TeamType           mTeamOnRightSide;
    private TextView           mLeftTeamNameText;
    private TextView           mRightTeamNameText;
    private MaterialButton     mSwapTeamsButton;
    private MaterialButton     mLeftTeamScoreButton;
    private MaterialButton     mRightTeamScoreButton;
    private TextView           mLeftTeamSetsText;
    private TextView           mRightTeamSetsText;
    private MaterialButton     mLeftTeamServiceButton;
    private MaterialButton     mRightTeamServiceButton;
    private TextView           mSetsText;
    private MaterialButton     mUndoGameEventButton;
    private MaterialButton     mLeftTeamTimeoutButton;
    private MaterialButton     mRightTeamTimeoutButton;
    private LinearLayout       mLeftTeamTimeoutLayout;
    private LinearLayout       mRightTeamTimeoutLayout;
    private MaterialButton     mLeftTeamCardsButton;
    private MaterialButton     mRightTeamCardsButton;
    private CountDown          mCountDown;

    public GameActivity() {
        super();
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof GameServiceHandler gameServiceHandler) {
                gameServiceHandler.setGameService(mGame);
            }
            if (fragment instanceof StoredGamesServiceHandler storedGamesServiceHandler) {
                storedGamesServiceHandler.setStoredGamesService(mStoredGamesService);
            }
        });
    }

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
        } else {
            long duration = savedInstanceState.getLong("saved_timeout_duration");
            startToolbarCountDown(duration);
        }

        if (mGame == null || mStoredGamesService == null) {
            UiUtils.navigateBackToHome(this);
        } else {
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
                mLeftTeamTimeoutButton.setVisibility(View.GONE);
                mRightTeamTimeoutButton.setVisibility(View.GONE);

                mLeftTeamTimeoutLayout.setVisibility(View.GONE);
                mRightTeamTimeoutLayout.setVisibility(View.GONE);
            }

            if (!mGame.getRules().isSanctions() || !UsageType.NORMAL.equals(mGame.getUsage())) {
                mLeftTeamCardsButton.setVisibility(View.GONE);
                mRightTeamCardsButton.setVisibility(View.GONE);
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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                UiUtils.navigateToMainWithDialog(GameActivity.this, mGame);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGame != null) {
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
            builder
                    .setTitle(String.format(getString(R.string.timeout_title), mGame.getTeamName(teamType)))
                    .setMessage(getString(R.string.timeout_question));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                Log.i(Tags.GAME_UI, "User accepts the timeout");
                mGame.callTimeout(teamType);
            });
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> Log.i(Tags.GAME_UI, "User rejects the timeout"));
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        } else {
            UiUtils.showNotification(findViewById(R.id.activity_game_content),
                                     String.format(getString(R.string.all_timeouts_called), mGame.getTeamName(teamType)));
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
        UiUtils.colorTeamButton(this, leftColor, mLeftTeamTimeoutButton);
        UiUtils.colorTeamButton(this, leftColor, mLeftTeamCardsButton);

        // Right

        mRightTeamNameText.setText(mGame.getTeamName(mTeamOnRightSide));
        int rightColor = mGame.getTeamColor(mTeamOnRightSide);
        UiUtils.colorTeamButton(this, rightColor, mRightTeamScoreButton);
        UiUtils.colorTeamButton(this, rightColor, mRightTeamTimeoutButton);
        UiUtils.colorTeamButton(this, rightColor, mRightTeamCardsButton);

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
            mLeftTeamServiceButton.setVisibility(View.VISIBLE);
            mRightTeamServiceButton.setVisibility(View.GONE);
        } else {
            mRightTeamServiceButton.setVisibility(View.VISIBLE);
            mLeftTeamServiceButton.setVisibility(View.GONE);
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

            if (view instanceof ImageView imageView) {

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
        CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration,
                                                                                      String.format(getString(R.string.timeout_title),
                                                                                                    mGame.getTeamName(teamType)));
        timeoutFragment.show(getSupportFragmentManager(), "timeout");

        if (mGame.countRemainingTimeouts(teamType) == 0) {
            UiUtils.showNotification(findViewById(R.id.activity_game_content),
                                     String.format(getString(R.string.all_timeouts_called), mGame.getTeamName(teamType)));
        }
    }

    @Override
    public void onTechnicalTimeout(int duration) {
        if (mGame.areNotificationsEnabled()) {
            deleteToolbarCountdown();
            deleteFragmentCountdown();
            CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration,
                                                                                          getString(R.string.technical_timeout_title));
            timeoutFragment.show(getSupportFragmentManager(), "timeout");
        }
    }

    @Override
    public void onGameInterval(int duration) {
        if (mGame.areNotificationsEnabled()) {
            deleteToolbarCountdown();
            deleteFragmentCountdown();
            CountDownDialogFragment timeoutFragment = CountDownDialogFragment.newInstance(duration,
                                                                                          getString(R.string.game_interval_title));
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
        String sanctionText = switch (sanctionType) {
            case YELLOW, DELAY_WARNING -> getString(R.string.yellow_card);
            case RED, DELAY_PENALTY -> getString(R.string.red_card);
            case RED_EXPULSION -> getString(R.string.red_card_expulsion);
            case RED_DISQUALIFICATION -> getString(R.string.red_card_disqualification);
        };

        UiUtils.makeText(this, sanctionText, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUndoSanction(TeamType teamType, SanctionType sanctionType, int number) {}

    private void initGameNavigation(final BottomNavigationView gameNavigation, Bundle savedInstanceState) {
        gameNavigation.setOnItemSelectedListener(item -> {
            final Fragment fragment;

            int itemId = item.getItemId();
            if (itemId == R.id.court_position_tab) {
                fragment = switch (mGame.getKind()) {
                    case INDOOR_4X4 -> Indoor4x4CourtFragment.newInstance();
                    case BEACH -> BeachCourtFragment.newInstance();
                    case SNOW -> SnowCourtFragment.newInstance();
                    default -> IndoorCourtFragment.newInstance();
                };
            } else if (itemId == R.id.substitutions_tab) {
                fragment = SubstitutionsFragment.newInstance();
            } else if (itemId == R.id.timeouts_tab) {
                fragment = TimeoutsFragment.newInstance();
            } else if (itemId == R.id.sanctions_tab) {
                fragment = SanctionsFragment.newInstance();
            } else {
                fragment = LaddersFragment.newInstance();
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.game_container, fragment).commit();

            return true;
        });

        if (!mGame.getRules().isTeamTimeouts()) {
            gameNavigation.getMenu().removeItem(R.id.timeouts_tab);
        }

        if (!mGame.getRules().isSanctions() || UsageType.POINTS_SCOREBOARD.equals(mGame.getUsage())) {
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

    private void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }
}
