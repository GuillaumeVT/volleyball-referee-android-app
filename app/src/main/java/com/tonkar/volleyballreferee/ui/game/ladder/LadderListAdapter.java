package com.tonkar.volleyballreferee.ui.game.ladder;

import android.content.Context;
import android.content.res.Configuration;
import android.view.*;
import android.widget.*;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.sanction.IBaseSanction;
import com.tonkar.volleyballreferee.engine.game.score.IBaseScore;
import com.tonkar.volleyballreferee.engine.game.timeout.IBaseTimeout;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.*;

public class LadderListAdapter extends BaseAdapter {

    private static final int sMaxLadderItems = 100;

    static class ViewHolder {
        TextView             setNumberText;
        TextView             setScoreText;
        TextView             setDurationText;
        HorizontalScrollView setLadderScroll;
        TableRow             homeTeamLadder;
        TableRow             guestTeamLadder;
        TableRow             homeTeamEventLadder;
        TableRow             guestTeamEventLadder;
    }

    private final boolean        mReverseOrder;
    private final LayoutInflater mLayoutInflater;
    private final IBaseScore     mBaseScore;
    private final IBaseTeam      mBaseTeam;
    private final IBaseTimeout   mBaseTimeout;
    private final IBaseSanction  mBaseSanction;
    private final int            mCurrentNightMode;

    public LadderListAdapter(LayoutInflater layoutInflater,
                             IBaseScore baseScore,
                             IBaseTeam baseTeam,
                             IBaseTimeout baseTimeout,
                             IBaseSanction baseSanction,
                             boolean reverseOrder) {
        mLayoutInflater = layoutInflater;
        mBaseScore = baseScore;
        mBaseTeam = baseTeam;
        mReverseOrder = reverseOrder;
        mBaseTimeout = baseTimeout;
        mBaseSanction = baseSanction;
        mCurrentNightMode = mLayoutInflater.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }

    @Override
    public int getCount() {
        return mBaseScore.getNumberOfSets();
    }

    @Override
    public Object getItem(int index) {
        return null;
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup parent) {
        View setView = view;
        final ViewHolder viewHolder;

        if (setView == null) {
            setView = mLayoutInflater.inflate(R.layout.ladder_with_title_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.setNumberText = setView.findViewById(R.id.set_number_text);
            viewHolder.setScoreText = setView.findViewById(R.id.set_score_text);
            viewHolder.setDurationText = setView.findViewById(R.id.set_duration_text);
            viewHolder.setLadderScroll = setView.findViewById(R.id.set_ladder_scroll);
            viewHolder.homeTeamLadder = setView.findViewById(R.id.set_ladder_home_team_row);
            viewHolder.guestTeamLadder = setView.findViewById(R.id.set_ladder_guest_team_row);
            viewHolder.homeTeamEventLadder = setView.findViewById(R.id.set_ladder_home_team_event_row);
            viewHolder.guestTeamEventLadder = setView.findViewById(R.id.set_ladder_guest_team_event_row);

            createLadderRow(viewHolder.homeTeamLadder, mBaseTeam.getTeamColor(TeamType.HOME));
            createLadderRow(viewHolder.guestTeamLadder, mBaseTeam.getTeamColor(TeamType.GUEST));
            createLadderEventRow(viewHolder.homeTeamEventLadder);
            createLadderEventRow(viewHolder.guestTeamEventLadder);

            setView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) setView.getTag();
        }

        int actualIndex = mReverseOrder ? (mBaseScore.getNumberOfSets() - index - 1) : index;

        viewHolder.setNumberText.setText(String.format(setView.getContext().getString(R.string.set_number), actualIndex + 1));
        viewHolder.setScoreText.setText(UiUtils.formatScoreFromLocale(mBaseScore.getPoints(TeamType.HOME, actualIndex),
                                                                      mBaseScore.getPoints(TeamType.GUEST, actualIndex), true));
        viewHolder.setDurationText.setText(
                String.format(setView.getContext().getString(R.string.set_duration), mBaseScore.getSetDuration(actualIndex) / 60000L));

        List<LadderItem> ladderItems = createLadderItems(mBaseScore.getPointsLadder(actualIndex));
        addSubstitutions(TeamType.HOME, actualIndex, ladderItems);
        addSubstitutions(TeamType.GUEST, actualIndex, ladderItems);
        addTimeouts(TeamType.HOME, actualIndex, ladderItems);
        addTimeouts(TeamType.GUEST, actualIndex, ladderItems);
        addSanctions(TeamType.HOME, actualIndex, ladderItems);
        addSanctions(TeamType.GUEST, actualIndex, ladderItems);
        addService(actualIndex, ladderItems);
        fillLadder(viewHolder, ladderItems);

        if (mReverseOrder && index == 0) {
            viewHolder.setLadderScroll.post(() -> viewHolder.setLadderScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT));
        }

        return setView;
    }

    private void createLadderRow(TableRow ladderRow, int color) {
        int width = measureTextWidth();
        for (int index = 0; index < sMaxLadderItems; index++) {
            TextView text = (TextView) mLayoutInflater.inflate(R.layout.ladder_item, null);
            text.setLayoutParams(createTableRowLayoutParams(text.getContext(), width, index));
            UiUtils.colorTeamText(text.getContext(), color, text);
            ladderRow.addView(text);
            text.setVisibility(View.GONE);
        }
    }

    private void createLadderEventRow(TableRow ladderRow) {
        int width = measureTextWidth();
        for (int index = 0; index < sMaxLadderItems; index++) {
            ImageView image = (ImageView) mLayoutInflater.inflate(R.layout.simple_event_item, null);
            image.setLayoutParams(createTableRowLayoutParams(image.getContext(), width, index));
            ladderRow.addView(image);
            image.setVisibility(View.GONE);
        }
    }

    private List<LadderItem> createLadderItems(List<TeamType> ladder) {
        List<LadderItem> ladderItems = new ArrayList<>();
        int homeCount = 0;
        int guestCount = 0;

        for (int index = 0; index < ladder.size(); index++) {
            if (index < sMaxLadderItems) {
                final TeamType teamType = ladder.get(index);

                if (TeamType.HOME.equals(teamType)) {
                    homeCount++;
                } else {
                    guestCount++;
                }

                ladderItems.add(new LadderItem(teamType, homeCount, guestCount));
            }
        }

        return ladderItems;
    }

    private void addSubstitutions(TeamType teamType, int setIndex, List<LadderItem> ladderItems) {
        for (SubstitutionDto substitution : mBaseTeam.getSubstitutions(teamType, setIndex)) {
            int homePoints = substitution.getHomePoints();
            int guestPoints = substitution.getGuestPoints();

            for (LadderItem ladderItem : ladderItems) {
                if (ladderItem.getHomePoints() == homePoints && ladderItem.getGuestPoints() == guestPoints) {
                    ladderItem.addSubstitution(teamType, substitution);
                }
            }
        }
    }

    private void addTimeouts(TeamType teamType, int setIndex, List<LadderItem> ladderItems) {
        for (TimeoutDto timeout : mBaseTimeout.getCalledTimeouts(teamType, setIndex)) {
            int homePoints = timeout.getHomePoints();
            int guestPoints = timeout.getGuestPoints();

            for (LadderItem ladderItem : ladderItems) {
                if (ladderItem.getHomePoints() == homePoints && ladderItem.getGuestPoints() == guestPoints) {
                    ladderItem.addTimeout(teamType, timeout);
                }
            }
        }
    }

    private void addSanctions(TeamType teamType, int setIndex, List<LadderItem> ladderItems) {
        for (SanctionDto sanction : mBaseSanction.getAllSanctions(teamType, setIndex)) {
            int homePoints = sanction.getHomePoints();
            int guestPoints = sanction.getGuestPoints();

            for (LadderItem ladderItem : ladderItems) {
                if (ladderItem.getHomePoints() == homePoints && ladderItem.getGuestPoints() == guestPoints) {
                    ladderItem.addSanction(teamType, sanction);
                }
            }
        }
    }

    private void addService(int setIndex, List<LadderItem> ladderItems) {
        if (ladderItems.size() > 0) {
            ladderItems.get(0).setFirstService(mBaseScore.getFirstServingTeam(setIndex));
        }
    }

    private void fillLadder(ViewHolder viewHolder, List<LadderItem> ladderItems) {
        for (int index = 0; index < ladderItems.size(); index++) {
            if (index < sMaxLadderItems) {
                final LadderItem ladderItem = ladderItems.get(index);

                TextView homeText = (TextView) viewHolder.homeTeamLadder.getChildAt(index);
                TextView guestText = (TextView) viewHolder.guestTeamLadder.getChildAt(index);
                ImageView homeImage = (ImageView) viewHolder.homeTeamEventLadder.getChildAt(index);
                ImageView guestImage = (ImageView) viewHolder.guestTeamEventLadder.getChildAt(index);

                if (TeamType.HOME.equals(ladderItem.getTeamType())) {
                    homeText.setVisibility(View.VISIBLE);
                    guestText.setVisibility(View.INVISIBLE);
                    homeText.setText(UiUtils.formatNumberFromLocale(ladderItem.getHomePoints()));
                } else {
                    guestText.setVisibility(View.VISIBLE);
                    homeText.setVisibility(View.INVISIBLE);
                    guestText.setText(UiUtils.formatNumberFromLocale(ladderItem.getGuestPoints()));
                }

                if (ladderItem.hasEvent(TeamType.HOME)) {
                    homeImage.setVisibility(View.VISIBLE);
                    homeImage.setImageResource(getIcon(TeamType.HOME, ladderItem));
                    homeImage.setOnClickListener(view -> {
                        LadderEventsDialog ladderEventsDialog = new LadderEventsDialog(mLayoutInflater, mLayoutInflater.getContext(),
                                                                                       TeamType.HOME, ladderItem, mBaseTeam);
                        ladderEventsDialog.show();
                    });
                } else {
                    homeImage.setVisibility(View.INVISIBLE);
                }

                if (ladderItem.hasEvent(TeamType.GUEST)) {
                    guestImage.setVisibility(View.VISIBLE);
                    guestImage.setImageResource(getIcon(TeamType.GUEST, ladderItem));
                    guestImage.setOnClickListener(view -> {
                        LadderEventsDialog ladderEventsDialog = new LadderEventsDialog(mLayoutInflater, mLayoutInflater.getContext(),
                                                                                       TeamType.GUEST, ladderItem, mBaseTeam);
                        ladderEventsDialog.show();
                    });
                } else {
                    guestImage.setVisibility(View.INVISIBLE);
                }
            }
        }

        for (int index = ladderItems.size(); index < sMaxLadderItems; index++) {
            viewHolder.homeTeamLadder.getChildAt(index).setVisibility(View.GONE);
            viewHolder.guestTeamLadder.getChildAt(index).setVisibility(View.GONE);
            viewHolder.homeTeamEventLadder.getChildAt(index).setVisibility(View.GONE);
            viewHolder.guestTeamEventLadder.getChildAt(index).setVisibility(View.GONE);
        }
    }

    private TableRow.LayoutParams createTableRowLayoutParams(Context context, int width, int columnIndex) {
        TableRow.LayoutParams params = new TableRow.LayoutParams(width, TableRow.LayoutParams.WRAP_CONTENT);
        params.column = columnIndex;

        int margin = getTextMargin(context);
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.topMargin = margin;
        params.bottomMargin = margin;

        return params;
    }

    private int measureTextWidth() {
        TextView text = (TextView) mLayoutInflater.inflate(R.layout.ladder_item, null);
        text.setText("##");

        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        int margin = getTextMargin(text.getContext());
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.topMargin = margin;
        params.bottomMargin = margin;

        text.setLayoutParams(params);
        text.measure(0, 0);
        return text.getMeasuredWidth();
    }

    private int getTextMargin(Context context) {
        return (int) context.getResources().getDimension(R.dimen.tiny_margin_size) / 2;
    }

    private int getIcon(TeamType teamType, LadderItem ladderItem) {
        int id = 0;

        if (ladderItem.hasSeveralEvents(teamType)) {
            switch (mCurrentNightMode) {
                case Configuration.UI_MODE_NIGHT_NO -> id = R.drawable.ic_thumb_list;
                case Configuration.UI_MODE_NIGHT_YES -> id = R.drawable.ic_thumb_list_white;
            }
        } else if (ladderItem.hasSubstitutionEvents(teamType)) {
            id = R.drawable.ic_thumb_substitution;
        } else if (ladderItem.hasTimeoutEvents(teamType)) {
            id = switch (mCurrentNightMode) {
                case Configuration.UI_MODE_NIGHT_NO -> R.drawable.ic_thumb_timeout;
                case Configuration.UI_MODE_NIGHT_YES -> R.drawable.ic_thumb_timeout_white;
                default -> id;
            };
        } else if (ladderItem.hasSanctionEvents(teamType)) {
            id = R.drawable.ic_thumb_card;
        } else if (ladderItem.hasFirstService(teamType)) {
            id = switch (mCurrentNightMode) {
                case Configuration.UI_MODE_NIGHT_NO -> R.drawable.ic_thumb_service;
                case Configuration.UI_MODE_NIGHT_YES -> R.drawable.ic_thumb_service_white;
                default -> id;
            };
        }

        return id;
    }
}
