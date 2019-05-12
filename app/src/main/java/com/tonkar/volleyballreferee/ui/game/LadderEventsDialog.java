package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiSanction;
import com.tonkar.volleyballreferee.api.ApiSubstitution;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LadderEventsDialog {

    private       AlertDialog     mAlertDialog;
    private final LayoutInflater  mLayoutInflater;
    private final Context         mContext;
    private final TeamType        mTeamType;
    private final BaseTeamService mBaseTeamService;

    LadderEventsDialog(LayoutInflater layoutInflater, final Context context, final TeamType teamType, final LadderItem ladderItem, final BaseTeamService baseTeamService) {
        mLayoutInflater = layoutInflater;
        mContext = context;
        mTeamType = teamType;
        mBaseTeamService = baseTeamService;

        View view = layoutInflater.inflate(R.layout.ladder_events_dialog, null);

        TextView title = view.findViewById(R.id.ladder_dialog_title);
        title.setText(String.format(Locale.getDefault(), "%d - %d", ladderItem.getHomePoints(), ladderItem.getGuestPoints()));

        List<LadderEvent> ladderEvents = new ArrayList<>();

        if (TeamType.HOME.equals(mTeamType)) {
            if (ladderItem.getHomeTimeouts().size() > 0) {
                ladderEvents.add(new LadderEvent());
            }
            for (ApiSubstitution substitution : ladderItem.getHomeSubstitutions()) {
                ladderEvents.add(new LadderEvent(substitution));
            }
            for (ApiSanction sanction : ladderItem.getHomeSanctions()) {
                ladderEvents.add(new LadderEvent(sanction));
            }
        } else {
            if (ladderItem.getGuestTimeouts().size() > 0) {
                ladderEvents.add(new LadderEvent());
            }
            for (ApiSubstitution substitution : ladderItem.getGuestSubstitutions()) {
                ladderEvents.add(new LadderEvent(substitution));
            }
            for (ApiSanction sanction : ladderItem.getGuestSanctions()) {
                ladderEvents.add(new LadderEvent(sanction));
            }
        }

        ListView eventList = view.findViewById(R.id.ladder_event_list);
        LadderEventListAdapter eventListAdapter = new LadderEventListAdapter(context, ladderEvents);
        eventList.setAdapter(eventListAdapter);

        mAlertDialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();
    }

    public void show() {
        if (mAlertDialog != null) {
            mAlertDialog.show();
        }
    }

    private class LadderEventListAdapter extends ArrayAdapter<LadderEvent> {

        private final List<LadderEvent> mLadderEvents;

        private LadderEventListAdapter(Context context, List<LadderEvent> ladderEvents) {
            super(context, android.R.layout.simple_list_item_1, ladderEvents);
            mLadderEvents = ladderEvents;
        }

        @Override
        public int getCount() {
            return mLadderEvents.size();
        }

        @Override
        public LadderEvent getItem(int index) {
            return mLadderEvents.get(index);
        }

        @Override
        public long getItemId(int index) {
            return 0;
        }

        @Override
        public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
            LadderEvent ladderEvent = mLadderEvents.get(index);

            switch (ladderEvent.getEventType()) {
                case TIMEOUT:
                    view = createTimeoutEvent(ladderEvent);
                    break;
                case SUBSTITUTION:
                    view = createSubstitutionEvent(ladderEvent);
                    break;
                case SANCTION:
                    view = createSanctionEvent(ladderEvent);
                    break;
                default:
                    break;
            }

            return view;
        }

        private View createTimeoutEvent(LadderEvent ladderEvent) {
            ImageView imageView = (ImageView) mLayoutInflater.inflate(R.layout.ladder_event_item, null);
            imageView.setImageResource(R.drawable.ic_timeout);
            imageView.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(mContext, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
            return imageView;
        }

        private View createSubstitutionEvent(LadderEvent ladderEvent) {
            View substitutionView = mLayoutInflater.inflate(R.layout.substitution_list_item, null);

            TextView scoreText = substitutionView.findViewById(R.id.score_text);
            scoreText.setVisibility(View.GONE);
            TextView playerInText = substitutionView.findViewById(R.id.player_in_text);
            TextView playerOutText = substitutionView.findViewById(R.id.player_out_text);

            ApiSubstitution substitution = ladderEvent.getSubstitution();
            playerInText.setText(UiUtils.formatNumberFromLocale(substitution.getPlayerIn()));
            playerOutText.setText(UiUtils.formatNumberFromLocale(substitution.getPlayerOut()));

            UiUtils.styleTeamText(mContext, mBaseTeamService, mTeamType, substitution.getPlayerIn(), playerInText);
            UiUtils.styleTeamText(mContext, mBaseTeamService, mTeamType, substitution.getPlayerOut(), playerOutText);

            return substitutionView;
        }

        private View createSanctionEvent(LadderEvent ladderEvent) {
            View sanctionView = mLayoutInflater.inflate(R.layout.sanction_list_item, null);

            TextView setText = sanctionView.findViewById(R.id.set_text);
            setText.setVisibility(View.GONE);
            TextView scoreText = sanctionView.findViewById(R.id.score_text);
            scoreText.setVisibility(View.GONE);
            TextView playerText = sanctionView.findViewById(R.id.player_text);
            ImageView sanctionTypeImage = sanctionView.findViewById(R.id.sanction_type_image);

            ApiSanction sanction = ladderEvent.getSanction();
            UiUtils.setSanctionImage(sanctionTypeImage, sanction.getCard());

            if (sanction.isTeam()) {
                playerText.setVisibility(View.GONE);
            } else {
                if (sanction.isPlayer()) {
                    playerText.setText(UiUtils.formatNumberFromLocale(sanction.getNum()));
                } else if (sanction.isCoach()) {
                    playerText.setText(mContext.getString(R.string.coach_abbreviation));
                }

                UiUtils.styleTeamText(mContext, mBaseTeamService, mTeamType, sanction.getNum(), playerText);
            }

            return sanctionView;
        }
    }

    private enum EventType {
        TIMEOUT, SUBSTITUTION, SANCTION
    }

    @Getter @Setter
    private class LadderEvent {

        private final EventType       eventType;
        private final ApiSubstitution substitution;
        private final ApiSanction     sanction;

        LadderEvent() {
            this.eventType = EventType.TIMEOUT;
            this.substitution = null;
            this.sanction = null;
        }

        LadderEvent(ApiSubstitution substitution) {
            this.eventType = EventType.SUBSTITUTION;
            this.substitution = substitution;
            this.sanction = null;
        }

        LadderEvent(ApiSanction sanction) {
            this.eventType = EventType.SANCTION;
            this.substitution = null;
            this.sanction = sanction;
        }

    }
}
