package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.*;

public class StoredGame extends GameDto implements IStoredGame {

    public StoredGame() {
        super();
        setHomeTeam(new TeamDto());
        setGuestTeam(new TeamDto());
        setRules(new RulesDto());
    }

    @Override
    public String getGameSummary() {
        if (GameStatus.COMPLETED.equals(getMatchStatus())) {
            return String.format(Locale.getDefault(), "%s\t\t%d\t-\t%d\t\t%s\n", getHomeTeam().getName(), getSets(TeamType.HOME),
                                 getSets(TeamType.GUEST), getGuestTeam().getName());
        } else {
            return String.format(Locale.getDefault(), "%s\t-\t%s\n", getHomeTeam().getName(), getGuestTeam().getName());
        }
    }

    public int currentSetIndex() {
        return getNumberOfSets() - 1;
    }

    public TeamDto getTeam(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeTeam() : getGuestTeam();
    }

    @Override
    public int getNumberOfSets() {
        return getSets().size();
    }

    @Override
    public int getSets(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeSets() : getGuestSets();
    }

    public void setSets(TeamType teamType, int count) {
        if (TeamType.HOME.equals(teamType)) {
            setHomeSets(count);
        } else {
            setGuestSets(count);
        }
    }

    @Override
    public int getPoints(TeamType teamType) {
        return getPoints(teamType, currentSetIndex());
    }

    @Override
    public int getPoints(TeamType teamType, int setIndex) {
        return getSets().get(setIndex).getPoints(teamType);
    }

    @Override
    public List<TeamType> getPointsLadder() {
        return getPointsLadder(currentSetIndex());
    }

    @Override
    public List<TeamType> getPointsLadder(int setIndex) {
        return getSets().get(setIndex).getLadder();
    }

    @Override
    public TeamType getServingTeam() {
        return getServingTeam(currentSetIndex());
    }

    @Override
    public TeamType getServingTeam(int setIndex) {
        return getSets().get(setIndex).getServing();
    }

    @Override
    public TeamType getFirstServingTeam() {
        return getFirstServingTeam(currentSetIndex());
    }

    @Override
    public TeamType getFirstServingTeam(int setIndex) {
        return getSets().get(setIndex).getFirstServing();
    }

    @Override
    public long getSetDuration(int setIndex) {
        return getSets().get(setIndex).getDuration();
    }

    @Override
    public long getSetStartTime(int setIndex) {
        return getSets().get(setIndex).getStartTime();
    }

    @Override
    public long getSetEndTime(int setIndex) {
        return getSets().get(setIndex).getEndTime();
    }

    @Override
    public GameStatus getMatchStatus() {
        return getStatus();
    }

    public void setMatchStatus(GameStatus gameStatus) {
        setStatus(gameStatus);
    }

    @Override
    public boolean isMatchCompleted() {
        return GameStatus.COMPLETED.equals(getMatchStatus());
    }

    @Override
    public Rules getRules() {
        Rules rules = new Rules();
        rules.setAll(super.getRules());
        return rules;
    }

    @Override
    public String getTeamId(TeamType teamType) {
        return getTeam(teamType).getId();
    }

    @Override
    public void setTeamId(TeamType teamType, String id) {}

    @Override
    public String getCreatedBy(TeamType teamType) {
        return getTeam(teamType).getCreatedBy();
    }

    @Override
    public void setCreatedBy(TeamType teamType, String createdBy) {}

    @Override
    public long getCreatedAt(TeamType teamType) {
        return getTeam(teamType).getCreatedAt();
    }

    @Override
    public void setCreatedAt(TeamType teamType, long createdAt) {}

    @Override
    public long getUpdatedAt(TeamType teamType) {
        return getTeam(teamType).getUpdatedAt();
    }

    @Override
    public void setUpdatedAt(TeamType teamType, long updatedAt) {}

    @Override
    public GameType getTeamsKind() {
        return getKind();
    }

    @Override
    public String getTeamName(TeamType teamType) {
        return getTeam(teamType).getName();
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        return getTeam(teamType).getColorInt();
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
            result = getHomeTeam().getPlayers().contains(new PlayerDto(number)) || getHomeTeam()
                    .getLiberos()
                    .contains(new PlayerDto(number));
        } else {
            result = getGuestTeam().getPlayers().contains(new PlayerDto(number)) || getGuestTeam()
                    .getLiberos()
                    .contains(new PlayerDto(number));
        }

        return result;
    }

    @Override
    public int getNumberOfPlayers(TeamType teamType) {
        int count;

        if (TeamType.HOME.equals(teamType)) {
            count = getHomeTeam().getPlayers().size() + getHomeTeam().getLiberos().size();
        } else {
            count = getGuestTeam().getPlayers().size() + getGuestTeam().getLiberos().size();
        }

        return count;
    }

    @Override
    public Set<PlayerDto> getPlayers(TeamType teamType) {
        Set<PlayerDto> players = new TreeSet<>();
        players.addAll(getTeam(teamType).getPlayers());
        players.addAll(getTeam(teamType).getLiberos());
        return players;
    }

    @Override
    public void setPlayerName(TeamType teamType, int number, String name) {}

    @Override
    public String getPlayerName(TeamType teamType, int number) {
        String playerName = "";

        for (PlayerDto player : getTeam(teamType).getPlayers()) {
            if (player.getNum() == number) {
                playerName = player.getName();
                break;
            }
        }
        for (PlayerDto player : getTeam(teamType).getLiberos()) {
            if (player.getNum() == number) {
                playerName = player.getName();
                break;
            }
        }

        return playerName;
    }

    @Override
    public GenderType getGender(TeamType teamType) {
        return getTeam(teamType).getGender();
    }

    @Override
    public void setGender(TeamType teamType, GenderType gender) {
        getTeam(teamType).setGender(gender);
    }

    @Override
    public int getExpectedNumberOfPlayersOnCourt() {
        return 0;
    }

    @Override
    public int getLiberoColor(TeamType teamType) {
        return getTeam(teamType).getLiberoColorInt();
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {}

    @Override
    public void addLibero(TeamType teamType, int number) {}

    @Override
    public void removeLibero(TeamType teamType, int number) {}

    @Override
    public boolean isLibero(TeamType teamType, int number) {
        return getTeam(teamType).getLiberos().contains(new PlayerDto(number));
    }

    @Override
    public boolean canAddLibero(TeamType teamType) {
        return false;
    }

    @Override
    public Set<PlayerDto> getLiberos(TeamType teamType) {
        return new TreeSet<>(getTeam(teamType).getLiberos());
    }

    @Override
    public List<SubstitutionDto> getSubstitutions(TeamType teamType) {
        return getSubstitutions(teamType, currentSetIndex());
    }

    @Override
    public List<SubstitutionDto> getSubstitutions(TeamType teamType, int setIndex) {
        return getSets().get(setIndex).getSubstitutions(teamType);
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType) {
        return isStartingLineupConfirmed(teamType, currentSetIndex());
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType, int setIndex) {
        SetDto set = getSets().get(setIndex);
        return set.getStartingPlayers(teamType).isFilled(getKind());
    }

    @Override
    public CourtDto getStartingLineup(TeamType teamType, int setIndex) {
        return getSets().get(setIndex).getStartingPlayers(teamType);
    }

    @Override
    public PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex) {
        CourtDto startingLineup = getStartingLineup(teamType, setIndex);
        return startingLineup.getPositionOf(number);
    }

    @Override
    public int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex) {
        CourtDto startingLineup = getStartingLineup(teamType, setIndex);
        return startingLineup.getPlayerAt(positionType);
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
        return new TreeSet<>();
    }

    @Override
    public boolean isCaptain(TeamType teamType, int number) {
        return number == getTeam(teamType).getCaptain();
    }

    @Override
    public String getCoachName(TeamType teamType) {
        return getTeam(teamType).getCoach();
    }

    @Override
    public void setCoachName(TeamType teamType, String name) {}

    @Override
    public int countRemainingTimeouts(TeamType teamType) {
        return getSets().get(currentSetIndex()).getTimeouts(teamType);
    }

    @Override
    public int countRemainingTimeouts(TeamType teamType, int setIndex) {
        return getSets().get(setIndex).getTimeouts(teamType);
    }

    @Override
    public List<TimeoutDto> getCalledTimeouts(TeamType teamType) {
        return getSets().get(currentSetIndex()).getCalledTimeouts(teamType);
    }

    @Override
    public List<TimeoutDto> getCalledTimeouts(TeamType teamType, int setIndex) {
        return getSets().get(setIndex).getCalledTimeouts(teamType);
    }

    @Override
    public long getRemainingTime() {
        return getSets().get(currentSetIndex()).getRemainingTime();
    }

    @Override
    public long getRemainingTime(int setIndex) {
        return getSets().get(setIndex).getRemainingTime();
    }

    @Override
    public int getGameCaptain(TeamType teamType, int setIndex) {
        return getSets().get(setIndex).getGameCaptain(teamType);
    }

    @Override
    public List<SanctionDto> getAllSanctions(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? getHomeCards() : getGuestCards();
    }

    @Override
    public List<SanctionDto> getAllSanctions(TeamType teamType, int setIndex) {
        List<SanctionDto> sanctionsForSet = new ArrayList<>();

        for (SanctionDto sanction : getAllSanctions(teamType)) {
            if (sanction.getSet() == setIndex) {
                sanctionsForSet.add(sanction);
            }
        }

        return sanctionsForSet;
    }

    @Override
    public List<SanctionDto> getPlayerSanctions(TeamType teamType, int number) {
        return new ArrayList<>();
    }

    @Override
    public boolean hasSanctions(TeamType teamType, int number) {
        return false;
    }

    @Override
    public List<TimeoutDto> getTimeoutsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints) {
        List<TimeoutDto> timeouts = new ArrayList<>();

        for (TimeoutDto timeout : getCalledTimeouts(teamType, setIndex)) {
            if (timeout.getHomePoints() == hPoints && timeout.getGuestPoints() == gPoints) {
                timeouts.add(timeout);
            }
        }

        return timeouts;
    }

    @Override
    public List<SubstitutionDto> getSubstitutionsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints) {
        List<SubstitutionDto> substitutions = new ArrayList<>();

        for (SubstitutionDto substitution : getSubstitutions(teamType, setIndex)) {
            if (substitution.getHomePoints() == hPoints && substitution.getGuestPoints() == gPoints) {
                substitutions.add(substitution);
            }
        }

        return substitutions;
    }

    @Override
    public List<SanctionDto> getSanctionsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints) {
        List<SanctionDto> sanctions = new ArrayList<>();

        for (SanctionDto sanction : getAllSanctions(teamType, setIndex)) {
            if (sanction.getHomePoints() == hPoints && sanction.getGuestPoints() == gPoints) {
                sanctions.add(sanction);
            }
        }

        return sanctions;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof StoredGame other) {
            result = super.equals(other);
        }

        return result;
    }
}
