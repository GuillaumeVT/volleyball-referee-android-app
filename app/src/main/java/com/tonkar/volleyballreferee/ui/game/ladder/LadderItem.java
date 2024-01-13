package com.tonkar.volleyballreferee.ui.game.ladder;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.*;

class LadderItem {

    private final TeamType              mTeamType;
    private final int                   mHomePoints;
    private final int                   mGuestPoints;
    private final List<ApiSubstitution> mHomeSubstitutions;
    private final List<ApiSubstitution> mGuestSubstitutions;
    private final List<ApiTimeout>      mHomeTimeouts;
    private final List<ApiTimeout>      mGuestTimeouts;
    private final List<ApiSanction>     mHomeSanctions;
    private final List<ApiSanction>     mGuestSanctions;
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

    List<ApiSubstitution> getHomeSubstitutions() {
        return mHomeSubstitutions;
    }

    List<ApiSubstitution> getGuestSubstitutions() {
        return mGuestSubstitutions;
    }

    List<ApiTimeout> getHomeTimeouts() {
        return mHomeTimeouts;
    }

    List<ApiTimeout> getGuestTimeouts() {
        return mGuestTimeouts;
    }

    List<ApiSanction> getHomeSanctions() {
        return mHomeSanctions;
    }

    List<ApiSanction> getGuestSanctions() {
        return mGuestSanctions;
    }

    void addSubstitution(TeamType teamType, ApiSubstitution substitution) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeSubstitutions.add(substitution);
        } else {
            mGuestSubstitutions.add(substitution);
        }
    }

    void addTimeout(TeamType teamType, ApiTimeout timeout) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeTimeouts.add(timeout);
        } else {
            mGuestTimeouts.add(timeout);
        }
    }

    void addSanction(TeamType teamType, ApiSanction sanction) {
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
