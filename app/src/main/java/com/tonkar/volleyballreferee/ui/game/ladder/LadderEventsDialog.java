package com.tonkar.volleyballreferee.ui.game.ladder;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameEvent;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.*;

public class LadderEventsDialog {

    private       AlertDialog    mAlertDialog;
    private final LayoutInflater mLayoutInflater;
    private final Context        mContext;
    private final IBaseTeam      mBaseTeam;

    LadderEventsDialog(LayoutInflater layoutInflater,
                       final Context context,
                       final TeamType teamType,
                       final LadderItem ladderItem,
                       final IBaseTeam baseTeam) {
        mLayoutInflater = layoutInflater;
        mContext = context;
        mBaseTeam = baseTeam;

        View view = layoutInflater.inflate(R.layout.ladder_events_dialog, null);

        TextView title = view.findViewById(R.id.ladder_dialog_title);
        title.setText(String.format(Locale.getDefault(), "%d - %d", ladderItem.getHomePoints(), ladderItem.getGuestPoints()));

        List<GameEvent> gameEvents = new ArrayList<>();

        if (TeamType.HOME.equals(teamType)) {
            if (ladderItem.getHomeTimeouts().size() > 0) {
                gameEvents.add(GameEvent.newTimeoutEvent(teamType));
            }
        } else {
            if (ladderItem.getGuestTimeouts().size() > 0) {
                gameEvents.add(GameEvent.newTimeoutEvent(teamType));
            }
        }

        for (SubstitutionDto substitution : (TeamType.HOME.equals(
                teamType) ? ladderItem.getHomeSubstitutions() : ladderItem.getGuestSubstitutions())) {
            gameEvents.add(GameEvent.newSubstitutionEvent(teamType, substitution));
        }
        for (SanctionDto sanction : (TeamType.HOME.equals(teamType) ? ladderItem.getHomeSanctions() : ladderItem.getGuestSanctions())) {
            gameEvents.add(GameEvent.newSanctionEvent(teamType, sanction));
        }

        ListView eventList = view.findViewById(R.id.ladder_event_list);
        GameEventListAdapter eventListAdapter = new GameEventListAdapter(context, gameEvents);
        eventList.setAdapter(eventListAdapter);

        mAlertDialog = new AlertDialog.Builder(context).setView(view).create();
    }

    public void show() {
        if (mAlertDialog != null) {
            mAlertDialog.show();
        }
    }

    private class GameEventListAdapter extends ArrayAdapter<GameEvent> {

        private final List<GameEvent> mGameEvents;

        private GameEventListAdapter(Context context, List<GameEvent> gameEvents) {
            super(context, android.R.layout.simple_list_item_1, gameEvents);
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
            view = mLayoutInflater.inflate(R.layout.game_event_item, null);

            GameEvent ladderEvent = mGameEvents.get(index);

            TextView textView = view.findViewById(R.id.game_event_text);
            LinearLayout layout = view.findViewById(R.id.game_event_layout);

            switch (ladderEvent.getEventType()) {
                case TIMEOUT -> {
                    textView.setText(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.timeout),
                                                   mBaseTeam.getTeamName(ladderEvent.getTeamType())));
                    layout.addView(createTimeoutEvent());
                }
                case SUBSTITUTION -> {
                    textView.setText(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.substitution),
                                                   mBaseTeam.getTeamName(ladderEvent.getTeamType())));
                    layout.addView(createSubstitutionEvent(ladderEvent));
                }
                case SANCTION -> {
                    if (ladderEvent.getSanction().getCard().isDelaySanctionType()) {
                        textView.setText(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.delay_sanction),
                                                       mBaseTeam.getTeamName(ladderEvent.getTeamType())));
                    } else {
                        textView.setText(String.format(Locale.getDefault(), "%s (%s)", mContext.getString(R.string.misconduct_sanction),
                                                       mBaseTeam.getTeamName(ladderEvent.getTeamType())));
                    }
                    layout.addView(createSanctionEvent(ladderEvent));
                }
            }

            return view;
        }

        private View createTimeoutEvent() {
            ImageView imageView = (ImageView) mLayoutInflater.inflate(R.layout.simple_event_item, null);
            imageView.setImageResource(R.drawable.ic_timeout);
            imageView
                    .getDrawable()
                    .mutate()
                    .setColorFilter(
                            new PorterDuffColorFilter(ContextCompat.getColor(mContext, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
            return imageView;
        }

        private View createSubstitutionEvent(GameEvent ladderEvent) {
            View substitutionView = mLayoutInflater.inflate(R.layout.substitution_list_item_no_score, null);

            TextView playerInText = substitutionView.findViewById(R.id.player_in_text);
            TextView playerOutText = substitutionView.findViewById(R.id.player_out_text);

            SubstitutionDto substitution = ladderEvent.getSubstitution();
            playerInText.setText(UiUtils.formatNumberFromLocale(substitution.getPlayerIn()));
            playerOutText.setText(UiUtils.formatNumberFromLocale(substitution.getPlayerOut()));

            UiUtils.styleTeamText(mContext, mBaseTeam, ladderEvent.getTeamType(), substitution.getPlayerIn(), playerInText);
            UiUtils.styleTeamText(mContext, mBaseTeam, ladderEvent.getTeamType(), substitution.getPlayerOut(), playerOutText);

            return substitutionView;
        }

        private View createSanctionEvent(GameEvent ladderEvent) {
            View sanctionView = mLayoutInflater.inflate(R.layout.sanction_list_item_no_score, null);

            TextView playerText = sanctionView.findViewById(R.id.player_text);
            ImageView sanctionTypeImage = sanctionView.findViewById(R.id.sanction_type_image);

            SanctionDto sanction = ladderEvent.getSanction();
            UiUtils.setSanctionImage(sanctionTypeImage, sanction.getCard());

            if (sanction.isTeam()) {
                playerText.setVisibility(View.GONE);
            } else {
                if (sanction.isPlayer()) {
                    playerText.setText(UiUtils.formatNumberFromLocale(sanction.getNum()));
                } else if (sanction.isCoach()) {
                    playerText.setText(mContext.getString(R.string.coach_abbreviation));
                }

                UiUtils.styleTeamText(mContext, mBaseTeam, ladderEvent.getTeamType(), sanction.getNum(), playerText);
            }

            return sanctionView;
        }
    }

}
