package com.tonkar.volleyballreferee.ui.setup;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.ApiGameSummary;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.game.GameFactory;
import com.tonkar.volleyballreferee.engine.game.GameStatus;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.service.AsyncGameRequestListener;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.service.StoredGamesManager;
import com.tonkar.volleyballreferee.engine.service.StoredGamesService;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

public class ScheduleGameListActionMenu extends BottomSheetDialogFragment implements AsyncGameRequestListener {

    private Activity           mActivity;
    private StoredGamesService mStoredGamesService;
    private ApiGameSummary     mGameDescription;
    private boolean            mConfigureBeforeStart;

    public static ScheduleGameListActionMenu newInstance(ApiGameSummary gameDescription) {
        ScheduleGameListActionMenu fragment = new ScheduleGameListActionMenu();
        Bundle args = new Bundle();
        args.putString("game", JsonConverters.GSON.toJson(gameDescription, ApiGameSummary.class));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String gameDescriptionStr = getArguments().getString("game");
        mGameDescription = JsonConverters.GSON.fromJson(gameDescriptionStr, ApiGameSummary.class);

        Log.i(Tags.SCHEDULE_UI, "Create schedule game list action menu fragment");
        View view = inflater
                .cloneInContext(new ContextThemeWrapper(inflater.getContext(), R.style.AppTheme_Dialog))
                .inflate(R.layout.schedule_game_list_action_menu, container, false);

        Context context = inflater.getContext();

        mActivity = getActivity();
        mStoredGamesService = new StoredGamesManager(mActivity);
        mConfigureBeforeStart = false;

        if (mActivity != null && mGameDescription != null) {
            TextView rescheduleGameText = view.findViewById(R.id.action_reschedule_match);
            View rescheduleGameSeparator = view.findViewById(R.id.reschedule_match_separator);
            TextView startGameText = view.findViewById(R.id.action_start_match);
            TextView editAndStartGameText = view.findViewById(R.id.action_edit_start_match);
            TextView deleteGameText = view.findViewById(R.id.action_delete_match);

            ApiUserSummary user = PrefUtils.getUser(mActivity);

            if (!mGameDescription.getCreatedBy().equals(user.getId())) {
                rescheduleGameText.setVisibility(View.GONE);
                rescheduleGameSeparator.setVisibility(View.GONE);
                deleteGameText.setVisibility(View.GONE);
            }

            if (GameStatus.LIVE.equals(mGameDescription.getStatus())) {
                rescheduleGameText.setVisibility(View.GONE);
                rescheduleGameSeparator.setVisibility(View.GONE);
                editAndStartGameText.setVisibility(View.GONE);
                startGameText.setText(R.string.resume_match);
            }

            rescheduleGameText.setOnClickListener(textView -> rescheduleGame());
            startGameText.setOnClickListener(textView -> startGame());
            editAndStartGameText.setOnClickListener(textView -> configureAndStartGame());
            deleteGameText.setOnClickListener(textView -> deleteGame());
        }

        return view;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog tmpDialog = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = tmpDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet).setHideable(true);
        });
        return bottomSheetDialog;
    }

    private void rescheduleGame() {
        Log.i(Tags.SCHEDULE_UI, "Reschedule game");
        if (!GameType.TIME.equals(mGameDescription.getKind())) {
            if (GameStatus.SCHEDULED.equals(mGameDescription.getStatus())) {
                switch (mGameDescription.getKind()) {
                    case INDOOR:
                    case INDOOR_4X4:
                    case BEACH:
                        Log.i(Tags.SCHEDULE_UI, "Start activity to reschedule game");
                        final Intent intent = new Intent(mActivity, ScheduledGameActivity.class);
                        intent.putExtra("game", JsonConverters.GSON.toJson(mGameDescription, ApiGameSummary.class));
                        intent.putExtra("create", false);
                        startActivity(intent);
                        UiUtils.animateForward(mActivity);
                        break;
                }
            }
        }
    }

    private void startGame() {
        Log.i(Tags.SCHEDULE_UI, "Start game");
        mConfigureBeforeStart = false;
        mStoredGamesService.downloadGame(mGameDescription.getId(),this);
    }

    private void configureAndStartGame() {
        Log.i(Tags.SCHEDULE_UI, "Configure and start game");
        mConfigureBeforeStart = true;
        mStoredGamesService.downloadGame(mGameDescription.getId(),this);
    }

    private void deleteGame() {
        Log.i(Tags.SCHEDULE_UI, "Delete game");
        mStoredGamesService.deleteGame(mGameDescription.getId());
        mActivity.recreate();
        dismiss();
    }

    @Override
    public void onGameReceived(IStoredGame storedGame) {
        if (storedGame != null) {
            mActivity.runOnUiThread(() -> {
                final IGame game = GameFactory.createGame(storedGame);
                Log.i(Tags.SCHEDULE_UI, "Received game");

                switch (storedGame.getMatchStatus()) {
                    case SCHEDULED:
                        if (mConfigureBeforeStart) {
                            Log.i(Tags.SCHEDULE_UI, "Edit scheduled game before starting");
                            mStoredGamesService.saveSetupGame(game);
                            final Intent setupIntent;
                            if (game.getKind().equals(GameType.BEACH)) {
                                setupIntent = new Intent(mActivity, QuickGameSetupActivity.class);
                            } else {
                                setupIntent = new Intent(mActivity, GameSetupActivity.class);
                            }
                            setupIntent.putExtra("create", false);
                            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(setupIntent);
                            UiUtils.animateForward(mActivity);
                        } else {
                            Log.i(Tags.SCHEDULE_UI, "Start scheduled game immediately");
                            game.startMatch();
                            mStoredGamesService.createCurrentGame(game);
                            final Intent gameIntent = new Intent(mActivity, GameActivity.class);
                            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(gameIntent);
                            UiUtils.animateCreate(mActivity);
                        }
                        break;
                    case LIVE:
                        Log.i(Tags.SCHEDULE_UI, "Resume game");
                        game.restoreGame(storedGame);
                        mStoredGamesService.createCurrentGame(game);
                        final Intent gameIntent = new Intent(mActivity, GameActivity.class);
                        gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(gameIntent);
                        UiUtils.animateCreate(mActivity);
                        break;
                    default:
                        break;
                }
            });
        }
    }

    @Override
    public void onAvailableGamesReceived(List<ApiGameSummary> gameDescriptionList) {}

    @Override
    public void onError(int httpCode) {
        mActivity.runOnUiThread(() -> UiUtils.makeErrorText(mActivity, getString(R.string.download_match_error), Toast.LENGTH_LONG).show());
    }

}
