package com.tonkar.volleyballreferee.ui.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.game.GameEvent;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSanction;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSubstitution;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.team.PlayerToggleButton;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;
import java.util.Locale;

public class UndoDialogFragment extends DialogFragment implements GameServiceHandler {

    private IGame                mGame;
    private LayoutInflater       mLayoutInflater;
    private AlertDialog          mAlertDialog;
    private View                 mView;
    private GameEventListAdapter mGameEventListAdapter;

    public static UndoDialogFragment newInstance() {
        return new UndoDialogFragment();
    }

    public UndoDialogFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mView;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        mLayoutInflater = getActivity().getLayoutInflater();
        mView = mLayoutInflater.inflate(R.layout.undo_dialog, null);

        ListView gameEventList = mView.findViewById(R.id.game_event_list);
        mGameEventListAdapter = new GameEventListAdapter(getContext(), mGame.getLatestGameEvents());
        gameEventList.setAdapter(mGameEventListAdapter);

        mAlertDialog = new AlertDialog
                .Builder(getContext(), R.style.AppTheme_Dialog)
                .setTitle(R.string.undo).setView(mView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> undo())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                .create();

        mAlertDialog.setOnShowListener(dialog -> computeOkAvailability());

        return mAlertDialog;
    }

    private void undo() {
        if (mGameEventListAdapter.getSelectedGameEvent() != null) {
            mGame.undoGameEvent(mGameEventListAdapter.getSelectedGameEvent());
        }
    }

    private void computeOkAvailability() {
        if (mAlertDialog != null && mGameEventListAdapter != null) {
            Button okButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            if (okButton != null) {
                okButton.setEnabled(mGameEventListAdapter.getSelectedGameEvent() != null);
            }
        }
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
    }

    private class GameEventListAdapter extends ArrayAdapter<GameEvent> {

        private Context            mContext;
        private List<GameEvent>    mGameEvents;
        private GameEvent          mSelectedGameEvent;
        private PlayerToggleButton mSelectedButton;

        private GameEventListAdapter(Context context, List<GameEvent> gameEvents) {
            super(context, R.layout.game_event_toggle_item, gameEvents);
            mContext = context;
            mGameEvents = gameEvents;
        }

        @Override
        public int getCount() {
            return mGameEvents.size();
        }

        @Override
        public GameEvent getItem(int index) {
            return mGameEvents.get(index);
        }

        @Override
        public long getItemId(int index) {
            return 0;
        }

        @Override
        public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
            view = mLayoutInflater.inflate(R.layout.game_event_toggle_item, null);

            GameEvent gameEvent = mGameEvents.get(index);

            PlayerToggleButton button = view.findViewById(R.id.game_event_button);
            LinearLayout layout = view.findViewById(R.id.game_event_layout);

            switch (gameEvent.getEventType()) {
                case POINT:
                    button.setText(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.remove_point_description), mGame.getTeamName(gameEvent.getTeamType())));
                    layout.addView(createPointEvent());
                    break;
                case TIMEOUT:
                    button.setText(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.timeout), mGame.getTeamName(gameEvent.getTeamType())));
                    layout.addView(createTimeoutEvent());
                    break;
                case SUBSTITUTION:
                    button.setText(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.substitution), mGame.getTeamName(gameEvent.getTeamType())));
                    layout.addView(createSubstitutionEvent(gameEvent));
                    break;
                case SANCTION:
                    if (gameEvent.getSanction().getCard().isDelaySanctionType()) {
                        button.setText(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.delay_sanction), mGame.getTeamName(gameEvent.getTeamType())));
                    } else {
                        button.setText(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.misconduct_sanction), mGame.getTeamName(gameEvent.getTeamType())));
                    }
                    layout.addView(createSanctionEvent(gameEvent));
                    break;
            }


            button.setColor(mContext, mGame.getTeamColor(gameEvent.getTeamType()));
            button.addOnCheckedChangeListener((cButton, isChecked) -> {
                UiUtils.animate(getContext(), cButton);
                if (isChecked) {
                    if (mSelectedButton != null) {
                        mSelectedButton.setChecked(false);
                    }
                    mSelectedButton = button;
                    mSelectedGameEvent = gameEvent;
                    computeOkAvailability();
                }
            });

            return view;
        }

        private View createPointEvent() {
            ImageView imageView = (ImageView) mLayoutInflater.inflate(R.layout.simple_event_item, null);
            imageView.setImageResource(R.drawable.ic_score_revert);
            imageView.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(mContext, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            return imageView;
        }

        private View createTimeoutEvent() {
            ImageView imageView = (ImageView) mLayoutInflater.inflate(R.layout.simple_event_item, null);
            imageView.setImageResource(R.drawable.ic_timeout);
            imageView.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(mContext, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            return imageView;
        }

        private View createSubstitutionEvent(GameEvent gameEvent) {
            View substitutionView = mLayoutInflater.inflate(R.layout.substitution_list_item_no_score, null);

            TextView playerInText = substitutionView.findViewById(R.id.player_in_text);
            TextView playerOutText = substitutionView.findViewById(R.id.player_out_text);

            ApiSubstitution substitution = gameEvent.getSubstitution();
            playerInText.setText(UiUtils.formatNumberFromLocale(substitution.getPlayerIn()));
            playerOutText.setText(UiUtils.formatNumberFromLocale(substitution.getPlayerOut()));

            UiUtils.styleTeamText(mContext, mGame, gameEvent.getTeamType(), substitution.getPlayerIn(), playerInText);
            UiUtils.styleTeamText(mContext, mGame, gameEvent.getTeamType(), substitution.getPlayerOut(), playerOutText);

            substitutionView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            return substitutionView;
        }

        private View createSanctionEvent(GameEvent gameEvent) {
            View sanctionView = mLayoutInflater.inflate(R.layout.sanction_list_item_no_score, null);

            TextView playerText = sanctionView.findViewById(R.id.player_text);
            ImageView sanctionTypeImage = sanctionView.findViewById(R.id.sanction_type_image);

            ApiSanction sanction = gameEvent.getSanction();
            UiUtils.setSanctionImage(sanctionTypeImage, sanction.getCard());

            if (sanction.isTeam()) {
                playerText.setVisibility(View.GONE);
            } else {
                if (sanction.isPlayer()) {
                    playerText.setText(UiUtils.formatNumberFromLocale(sanction.getNum()));
                } else if (sanction.isCoach()) {
                    playerText.setText(mContext.getString(R.string.coach_abbreviation));
                }

                UiUtils.styleTeamText(mContext, mGame, gameEvent.getTeamType(), sanction.getNum(), playerText);
            }

            sanctionView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            return sanctionView;
        }

        private GameEvent getSelectedGameEvent() {
            return mSelectedGameEvent;
        }
    }
}
