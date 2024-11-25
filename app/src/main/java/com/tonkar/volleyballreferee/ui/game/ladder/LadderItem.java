package com.tonkar.volleyballreferee.ui.game.ladder;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.*;

class LadderItem {

    private final TeamType              mTeamType;
    private final int                   mHomePoints;
    private final int                   mGuestPoints;
    private final List<SubstitutionDto> mHomeSubstitutions;
    private final List<SubstitutionDto> mGuestSubstitutions;
    private final List<TimeoutDto>      mHomeTimeouts;
    private final List<TimeoutDto>      mGuestTimeouts;
    private final List<SanctionDto>     mHomeSanctions;
    private final List<SanctionDto>     mGuestSanctions;
    private       TeamType              mFirstService;

    LadderItem(TeamType teamType, int homePoints, int guestPoints) {
        mTeamType = teamType;
        mHomePoints = homePoints;
        mGuestPoints = guestPoints;
        mHomeSubstitutions = new ArrayList<>();
        mGuestSubstitutions = new ArrayList<>();
        mHomeTimeouts = new ArrayList<>();
        mGuestTimeouts = new ArrayList<>();
        mHomeSanctions = new ArrayList<>();
        mGuestSanctions = new ArrayList<>();
    }

    TeamType getTeamType() {
        return mTeamType;
    }

    int getHomePoints() {
        return mHomePoints;
    }

    int getGuestPoints() {
        return mGuestPoints;
    }

    List<SubstitutionDto> getHomeSubstitutions() {
        return mHomeSubstitutions;
    }

    List<SubstitutionDto> getGuestSubstitutions() {
        return mGuestSubstitutions;
    }

    List<TimeoutDto> getHomeTimeouts() {
        return mHomeTimeouts;
    }

    List<TimeoutDto> getGuestTimeouts() {
        return mGuestTimeouts;
    }

    List<SanctionDto> getHomeSanctions() {
        return mHomeSanctions;
    }

    List<SanctionDto> getGuestSanctions() {
        return mGuestSanctions;
    }

    void addSubstitution(TeamType teamType, SubstitutionDto substitution) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeSubstitutions.add(substitution);
        } else {
            mGuestSubstitutions.add(substitution);
        }
    }

    void addTimeout(TeamType teamType, TimeoutDto timeout) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeTimeouts.add(timeout);
        } else {
            mGuestTimeouts.add(timeout);
        }
    }

    void addSanction(TeamType teamType, SanctionDto sanction) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeSanctions.add(sanction);
        } else {
            mGuestSanctions.add(sanction);
        }
    }

    void setFirstService(TeamType firstService) {
        mFirstService = firstService;
    }

    boolean hasSubstitutionEvents(TeamType teamType) {
        if (TeamType.HOME.equals(teamType)) {
            return mHomeSubstitutions.size() > 0;
        } else {
            return mGuestSubstitutions.size() > 0;
        }
    }

    boolean hasTimeoutEvents(TeamType teamType) {
        if (TeamType.HOME.equals(teamType)) {
            return mHomeTimeouts.size() > 0;
        } else {
            return mGuestTimeouts.size() > 0;
        }
    }

    boolean hasSanctionEvents(TeamType teamType) {
        if (TeamType.HOME.equals(teamType)) {
            return mHomeSanctions.size() > 0;
        } else {
            return mGuestSanctions.size() > 0;
        }
    }

    boolean hasFirstService(TeamType teamType) {
        return teamType.equals(mFirstService);
    }

    boolean hasEvent(TeamType teamType) {
        return hasSubstitutionEvents(teamType) || hasTimeoutEvents(teamType) || hasSanctionEvents(teamType) || hasFirstService(teamType);
    }

    boolean hasSeveralEvents(TeamType teamType) {
        int count = 0;

        if (hasSubstitutionEvents(teamType)) {
            count++;
        }
        if (hasTimeoutEvents(teamType)) {
            count++;
        }
        if (hasSanctionEvents(teamType)) {
            count++;
        }
        if (hasFirstService(teamType)) {
            count++;
        }

        return count > 1;
    }
}
