package com.tonkar.volleyballreferee.ui.setup;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiGameDescription;
import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.JsonIOUtils;
import com.tonkar.volleyballreferee.business.data.StoredGames;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.AsyncGameRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.interfaces.data.StoredGamesService;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

public class ScheduleGameListActionMenu extends BottomSheetDialogFragment implements AsyncGameRequestListener {

    private Activity           mActivity;
    private StoredGamesService mStoredGamesService;
    private ApiGameDescription mGameDescription;
    private boolean            mConfigureBeforeStart;

    public static ScheduleGameListActionMenu newInstance(ApiGameDescription gameDescription) {
        ScheduleGameListActionMenu fragment = new ScheduleGameListActionMenu();
        Bundle args = new Bundle();
        args.putString("game", JsonIOUtils.GSON.toJson(gameDescription, JsonIOUtils.GAME_DESCRIPTION_TYPE));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String gameDescriptionStr = getArguments().getString("game");
        mGameDescription = JsonIOUtils.GSON.fromJson(gameDescriptionStr, JsonIOUtils.GAME_DESCRIPTION_TYPE);

        Log.i(Tags.SCHEDULE_UI, "Create schedule game list action menu fragment");
        View view = inflater
                .cloneInContext(new ContextThemeWrapper(inflater.getContext(), R.style.AppTheme_Dialog))
                .inflate(R.layout.schedule_game_list_action_menu, container, false);

        Context context = inflater.getContext();

        mActivity = getActivity();
        mStoredGamesService = new StoredGames(mActivity);
        mConfigureBeforeStart = false;

        if (mActivity != null && mGameDescription != null) {
            TextView rescheduleGameText = view.findViewById(R.id.action_reschedule_match);
            TextView startGameText = view.findViewById(R.id.action_start_match);
            TextView editAndStartGameText = view.findViewById(R.id.action_edit_start_match);
            TextView deleteGameText = view.findViewById(R.id.action_delete_match);

            Authentication authentication = PrefUtils.getAuthentication(mActivity);

            if (!mGameDescription.getCreatedBy().equals(authentication.getUserId())) {
                rescheduleGameText.setVisibility(View.GONE);
                deleteGameText.setVisibility(View.GONE);
            }

            if (GameStatus.LIVE.equals(mGameDescription.getStatus())) {
                rescheduleGameText.setVisibility(View.GONE);
                editAndStartGameText.setVisibility(View.GONE);
                startGameText.setText(R.string.resume_match);
            }

            UiUtils.setDrawableStart(rescheduleGameText, R.drawable.ic_schedule);
            UiUtils.setDrawableStart(startGameText, R.drawable.ic_play);
            UiUtils.setDrawableStart(editAndStartGameText, R.drawable.ic_edit_and_play);
            UiUtils.setDrawableStart(deleteGameText, R.drawable.ic_delete);

            colorIcon(context, rescheduleGameText);
            colorIcon(context, startGameText);
            colorIcon(context, editAndStartGameText);
            colorIcon(context, deleteGameText);

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
                        intent.putExtra("game", JsonIOUtils.GSON.toJson(mGameDescription, JsonIOUtils.GAME_DESCRIPTION_TYPE));
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

    private void colorIcon(Context context, TextView textView) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    @Override
    public void onGameReceived(StoredGameService storedGameService) {
        if (storedGameService != null) {
            final GameService gameService = GameFactory.createGame(storedGameService);
            Log.i(Tags.SCHEDULE_UI, "Received game");

            switch (storedGameService.getMatchStatus()) {
                case SCHEDULED:
                    if (mConfigureBeforeStart) {
                        Log.i(Tags.SCHEDULE_UI, "Edit scheduled game before starting");
                        mStoredGamesService.saveSetupGame(gameService);
                        final Intent setupIntent;
                        if (gameService.getKind().equals(GameType.BEACH)) {
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
                        gameService.startMatch();
                        mStoredGamesService.createCurrentGame(gameService);
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
                    gameService.restoreGame(storedGameService);
                    mStoredGamesService.createCurrentGame(gameService);
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
        }
    }

    @Override
    public void onAvailableGamesReceived(List<ApiGameDescription> gameDescriptionList) {}

    @Override
    public void onError(int httpCode) {
        UiUtils.makeErrorText(mActivity, getString(R.string.download_match_error), Toast.LENGTH_LONG).show();
    }

}
