package com.tonkar.volleyballreferee.ui.game;

import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.timeout.Timeout;

import java.util.ArrayList;
import java.util.List;

public class LadderItem {

    private       TeamType           mTeamType;
    private       int                mHomePoints;
    private       int                mGuestPoints;
    private final List<Substitution> mHomeSubstitutions;
    private final List<Substitution> mGuestSubstitutions;
    private final List<Timeout>      mHomeTimeouts;
    private final List<Timeout>      mGuestTimeouts;
    private final List<Sanction>     mHomeSanctions;
    private final List<Sanction>     mGuestSanctions;

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

    List<Substitution> getHomeSubstitutions() {
        return mHomeSubstitutions;
    }

    List<Substitution> getGuestSubstitutions() {
        return mGuestSubstitutions;
    }

    List<Timeout> getHomeTimeouts() {
        return mHomeTimeouts;
    }

    List<Timeout> getGuestTimeouts() {
        return mGuestTimeouts;
    }

    List<Sanction> getHomeSanctions() {
        return mHomeSanctions;
    }

    List<Sanction> getGuestSanctions() {
        return mGuestSanctions;
    }

    void addSubstitution(TeamType teamType, Substitution substitution) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeSubstitutions.add(substitution);
        } else {
            mGuestSubstitutions.add(substitution);
        }
    }

    void addTimeout(TeamType teamType, Timeout timeout) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeTimeouts.add(timeout);
        } else {
            mGuestTimeouts.add(timeout);
        }
    }

    void addSanction(TeamType teamType, Sanction sanction) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeSanctions.add(sanction);
        } else {
            mGuestSanctions.add(sanction);
        }
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

    boolean hasEvent(TeamType teamType) {
        return hasSubstitutionEvents(teamType) || hasTimeoutEvents(teamType) || hasSanctionEvents(teamType);
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

        return count > 1;
    }
}
