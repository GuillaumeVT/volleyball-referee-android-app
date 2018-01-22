package com.tonkar.volleyballreferee.business.data;

import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.Timeout;
import com.tonkar.volleyballreferee.interfaces.UsageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class RecordedGame implements RecordedGameService {

    private GameType          mGameType;
    private long              mGameDate;
    private GenderType        mGenderType;
    private UsageType         mUsageType;
    private boolean           mLive;
    private String            mLeagueName;
    private RecordedTeam      mHomeTeam;
    private RecordedTeam      mGuestTeam;
    private int               mHomeSets;
    private int               mGuestSets;
    private List<RecordedSet> mSets;

    public RecordedGame() {
        mGameType = GameType.INDOOR;
        mGameDate = 0L;
        mGenderType = GenderType.MIXED;
        mUsageType = UsageType.NORMAL;
        mLive = false;
        mLeagueName = "";
        mHomeTeam = new RecordedTeam();
        mGuestTeam = new RecordedTeam();
        mHomeSets = 0;
        mGuestSets = 0;
        mSets = new ArrayList<>();
    }

    @Override
    public String getGameSummary() {
        return String.format(Locale.getDefault(),"%s\t\t%d\t-\t%d\t\t%s\n", mHomeTeam.getName(), getSets(TeamType.HOME), getSets(TeamType.GUEST), mGuestTeam.getName());
    }

    private int currentSetIndex() {
        return mSets.size() -1;
    }

    public RecordedTeam getTeam(TeamType teamType) {
        RecordedTeam team;

        if (TeamType.HOME.equals(teamType)) {
            team = mHomeTeam;
        } else {
            team = mGuestTeam;
        }

        return team;
    }

    public List<RecordedSet> getSets() {
        return mSets;
    }

    @Override
    public GameType getGameType() {
        return mGameType;
    }

    public void setGameType(GameType gameType) {
        mGameType = gameType;
    }

    @Override
    public long getGameDate() {
        return mGameDate;
    }

    public void setGameDate(long gameDate) {
        mGameDate = gameDate;
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

    public void setSets(TeamType teamType, int count) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeSets = count;
        } else {
            mGuestSets = count;
        }
    }

    @Override
    public long getSetDuration(int setIndex) {
        return mSets.get(setIndex).getDuration();
    }

    @Override
    public int getPoints(TeamType teamType) {
        return getPoints(teamType, currentSetIndex());
    }

    @Override
    public int getPoints(TeamType teamType, int setIndex) {
        return mSets.get(setIndex).getPoints(teamType);
    }

    @Override
    public List<TeamType> getPointsLadder() {
        return getPointsLadder(currentSetIndex());
    }

    @Override
    public List<TeamType> getPointsLadder(int setIndex) {
        return mSets.get(setIndex).getPointsLadder();
    }

    @Override
    public TeamType getServingTeam() {
        return getServingTeam(currentSetIndex());
    }

    @Override
    public TeamType getServingTeam(int setIndex) {
        return mSets.get(setIndex).getServingTeam();
    }

    @Override
    public boolean isMatchCompleted() {
        return !mLive;
    }

    @Override
    public UsageType getUsageType() {
        return mUsageType;
    }

    @Override
    public void setUsageType(UsageType usageType) {
        mUsageType = usageType;
    }

    public void setMatchCompleted(boolean matchCompleted) {
        mLive = !matchCompleted;
    }

    @Override
    public String getLeagueName() {
        return mLeagueName;
    }

    @Override
    public void setLeagueName(String name) {
        mLeagueName = name;
    }

    @Override
    public String getTeamName(TeamType teamType) {
        return getTeam(teamType).getName();
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        return getTeam(teamType).getColor();
    }

    @Override
    public void setTeamName(TeamType teamType, String name) {}

    @Override
    public void setTeamColor(TeamType teamType, int color) {}

    @Override
    public void addPlayer(TeamType teamType, int number) {}

    @Override
    public void removePlayer(TeamType teamType, int number) {}

    @Override
    public boolean hasPlayer(TeamType teamType, int number) {
        boolean result;

        if (TeamType.HOME.equals(teamType)) {
            result = mHomeTeam.getPlayers().contains(number) || mHomeTeam.getLiberos().contains(number);
        } else {
            result = mGuestTeam.getPlayers().contains(number) || mGuestTeam.getLiberos().contains(number);
        }

        return result;
    }

    @Override
    public int getNumberOfPlayers(TeamType teamType) {
        int count;

        if (TeamType.HOME.equals(teamType)) {
            count = mHomeTeam.getPlayers().size() + mHomeTeam.getLiberos().size();
        } else {
            count = mGuestTeam.getPlayers().size() + mGuestTeam.getLiberos().size();
        }

        return count;
    }

    @Override
    public Set<Integer> getPlayers(TeamType teamType) {
        Set<Integer> players = new TreeSet<>();
        players.addAll(getTeam(teamType).getPlayers());
        players.addAll(getTeam(teamType).getLiberos());
        return players;
    }

    @Override
    public GenderType getGenderType() {
        return mGenderType;
    }

    @Override
    public GenderType getGenderType(TeamType teamType) {
        return getTeam(teamType).getGenderType();
    }

    @Override
    public void setGenderType(GenderType genderType) {
        mGenderType = genderType;
    }

    @Override
    public void setGenderType(TeamType teamType, GenderType genderType) {
        getTeam(teamType).setGenderType(genderType);
    }

    @Override
    public int getLiberoColor(TeamType teamType) {
        return getTeam(teamType).getLiberoColor();
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {}

    @Override
    public void addLibero(TeamType teamType, int number) {}

    @Override
    public void removeLibero(TeamType teamType, int number) {}

    @Override
    public boolean isLibero(TeamType teamType, int number) {
        return getTeam(teamType).getLiberos().contains(number);
    }

    @Override
    public boolean canAddLibero(TeamType teamType) {
        return false;
    }

    @Override
    public Set<Integer> getLiberos(TeamType teamType) {
        return getTeam(teamType).getLiberos();
    }

    @Override
    public List<Substitution> getSubstitutions(TeamType teamType) {
        return getSubstitutions(teamType, currentSetIndex());
    }

    @Override
    public List<Substitution> getSubstitutions(TeamType teamType, int setIndex) {
        return mSets.get(setIndex).getSubstitutions(teamType);
    }

    @Override
    public boolean isStartingLineupConfirmed() {
        RecordedSet set = mSets.get(currentSetIndex());
        return !set.getStartingPlayers(TeamType.HOME).isEmpty() && !set.getStartingPlayers(TeamType.GUEST).isEmpty();
    }

    @Override
    public Set<Integer> getPlayersInStartingLineup(TeamType teamType, int setIndex) {
        Set<Integer> players = new TreeSet<>();
        List<RecordedPlayer> startingLineup = mSets.get(setIndex).getStartingPlayers(teamType);

        for (RecordedPlayer player : startingLineup) {
            players.add(player.getNumber());
        }

        return players;
    }

    @Override
    public PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex) {
        PositionType positionType = null;
        List<RecordedPlayer> startingLineup = mSets.get(setIndex).getStartingPlayers(teamType);

        for (RecordedPlayer player : startingLineup) {
            if (player.getNumber() == number) {
                positionType = player.getPositionType();
            }
        }

        return positionType;
    }

    @Override
    public int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex) {
        int number = -1;
        List<RecordedPlayer> startingLineup = mSets.get(setIndex).getStartingPlayers(teamType);

        for (RecordedPlayer player : startingLineup) {
            if (player.getPositionType().equals(positionType)) {
                number = player.getNumber();
            }
        }

        return number;
    }

    @Override
    public void setCaptain(TeamType teamType, int number) {
        getTeam(teamType).setCaptain(number);
    }

    @Override
    public int getCaptain(TeamType teamType) {
        return getTeam(teamType).getCaptain();
    }

    @Override
    public Set<Integer> getPossibleCaptains(TeamType teamType) {
        Set<Integer> possibleCaptains = new TreeSet<>();

        for (int number : getTeam(teamType).getPlayers()) {
            if (!isLibero(teamType, number)) {
                possibleCaptains.add(number);
            }
        }

        return possibleCaptains;
    }

    @Override
    public boolean isCaptain(TeamType teamType, int number) {
        return number == getTeam(teamType).getCaptain();
    }

    @Override
    public void initTeams() {}

    @Override
    public int getRemainingTimeouts(TeamType teamType) {
        return mSets.get(currentSetIndex()).getTimeouts(teamType);
    }

    @Override
    public int getRemainingTimeouts(TeamType teamType, int setIndex) {
        return mSets.get(setIndex).getTimeouts(teamType);
    }

    @Override
    public List<Timeout> getCalledTimeouts(TeamType teamType) {
        return mSets.get(currentSetIndex()).getCalledTimeouts(teamType);
    }

    @Override
    public List<Timeout> getCalledTimeouts(TeamType teamType, int setIndex) {
        return mSets.get(setIndex).getCalledTimeouts(teamType);
    }

    @Override
    public long getRemainingTime() {
        return mSets.get(currentSetIndex()).getRemainingTime();
    }

    @Override
    public long getRemainingTime(int setIndex) {
        return mSets.get(setIndex).getRemainingTime();
    }

    @Override
    public boolean matchesFilter(String text) {
        return  text.isEmpty()
                || mHomeTeam.getName().toLowerCase(Locale.getDefault()).contains(text)
                || mGuestTeam.getName().toLowerCase(Locale.getDefault()).contains(text)
                || mLeagueName.toLowerCase(Locale.getDefault()).contains(text);
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
                    && (this.getGenderType().equals(other.getGenderType()))
                    && (this.getUsageType().equals(other.getUsageType()))
                    && (this.isMatchCompleted() == other.isMatchCompleted())
                    && (this.getLeagueName().equals(other.getLeagueName()))
                    && this.getTeam(TeamType.HOME).equals(other.getTeam(TeamType.HOME))
                    && this.getTeam(TeamType.GUEST).equals(other.getTeam(TeamType.GUEST))
                    && (this.getSets(TeamType.HOME) == other.getSets(TeamType.HOME))
                    && (this.getSets(TeamType.GUEST) == other.getSets(TeamType.GUEST))
                    && this.getSets().equals(other.getSets());
        }

        return result;
    }

}
