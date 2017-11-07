package com.tonkar.volleyballreferee.business.history;

import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.util.List;
import java.util.Locale;

public class RecordedGame implements RecordedGameService {

    private final GameType          mGameType;
    private final long              mGameDate;
    private final RecordedTeam      mHomeTeam;
    private final RecordedTeam      mGuestTeam;
    private final List<RecordedSet> mSets;
    private final int               mHomeSets;
    private final int               mGuestSets;

    public RecordedGame(GameType gameType, long gameDate, RecordedTeam homeTeam, RecordedTeam guestTeam, List<RecordedSet> sets) {
        mGameType = gameType;
        mGameDate = gameDate;
        mHomeTeam = homeTeam;
        mGuestTeam = guestTeam;
        mSets = sets;

        int homeSets = 0;
        int guestSets = 0;

        for (RecordedSet recordedSet : mSets) {
            if (recordedSet.getHomeTeamPoints() > recordedSet.getGuestTeamPoints()) {
                homeSets++;
            }
            else {
                guestSets++;
            }
        }

        mHomeSets = homeSets;
        mGuestSets = guestSets;
    }

    RecordedTeam getHomeTeam() {
        return mHomeTeam;
    }

    RecordedTeam getGuestTeam() {
        return mGuestTeam;
    }

    List<RecordedSet> getSets() {
        return mSets;
    }

    @Override
    public GameType getGameType() {
        return mGameType;
    }

    @Override
    public long getGameDate() {
        return mGameDate;
    }

    @Override
    public String getGameSummary() {
        return String.format(Locale.getDefault(),"%s\t\t%d\t-\t%d\t\t%s\n", mHomeTeam.getName(), getSets(TeamType.HOME),getSets(TeamType.GUEST), mGuestTeam.getName());
    }

    @Override
    public int getNumberOfSets() {
        return mSets.size();
    }

    @Override
    public int getSets(TeamType teamType) {
        int count;

        if (TeamType.HOME.equals(teamType)) {
            count = mHomeSets;
        } else {
            count = mGuestSets;
        }

        return count;
    }

    @Override
    public long getSetDuration() {
        return getSetDuration(mSets.size() -1);
    }

    @Override
    public long getSetDuration(int setIndex) {
        return mSets.get(setIndex).getDuration();
    }

    @Override
    public int getPoints(TeamType teamType) {
        return getPoints(teamType, mSets.size() -1);
    }

    @Override
    public int getPoints(TeamType teamType, int setIndex) {
        int count;

        if (TeamType.HOME.equals(teamType)) {
            count = mSets.get(setIndex).getHomeTeamPoints();
        } else {
            count = mSets.get(setIndex).getGuestTeamPoints();
        }

        return count;
    }

    @Override
    public List<TeamType> getPointsLadder() {
        return getPointsLadder(mSets.size() -1);
    }

    @Override
    public List<TeamType> getPointsLadder(int setIndex) {
        return mSets.get(setIndex).getPointsLadder();
    }

    @Override
    public String getTeamName(TeamType teamType) {
        String name;

        if (TeamType.HOME.equals(teamType)) {
            name = mHomeTeam.getName();
        } else {
            name = mGuestTeam.getName();
        }

        return name;
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        int colorId;

        if (TeamType.HOME.equals(teamType)) {
            colorId = mHomeTeam.getColorId();
        } else {
            colorId = mGuestTeam.getColorId();
        }

        return colorId;
    }

    @Override
    public boolean matchesFilter(String text) {
        return  text.isEmpty()
                || mHomeTeam.getName().toLowerCase(Locale.getDefault()).contains(text)
                || mGuestTeam.getName().toLowerCase(Locale.getDefault()).contains(text);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof RecordedGame) {
            RecordedGame other = (RecordedGame) obj;
            result = (this.getGameDate() == other.getGameDate())
                    && this.getGameType().equals(other.getGameType())
                    && this.getHomeTeam().equals(other.getHomeTeam())
                    && this.getGuestTeam().equals(other.getGuestTeam())
                    && this.getSets().equals(other.getSets());
        }

        return result;
    }
}
