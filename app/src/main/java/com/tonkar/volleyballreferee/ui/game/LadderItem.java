package com.tonkar.volleyballreferee.ui.game;

import com.tonkar.volleyballreferee.interfaces.card.PenaltyCard;
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
    private final List<PenaltyCard>  mHomePenaltyCards;
    private final List<PenaltyCard>  mGuestPenaltyCards;

    LadderItem(TeamType teamType, int homePoints, int guestPoints) {
        mTeamType = teamType;
        mHomePoints = homePoints;
        mGuestPoints = guestPoints;
        mHomeSubstitutions = new ArrayList<>();
        mGuestSubstitutions = new ArrayList<>();
        mHomeTimeouts = new ArrayList<>();
        mGuestTimeouts = new ArrayList<>();
        mHomePenaltyCards = new ArrayList<>();
        mGuestPenaltyCards = new ArrayList<>();
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

    List<PenaltyCard> getHomePenaltyCards() {
        return mHomePenaltyCards;
    }

    List<PenaltyCard> getGuestPenaltyCards() {
        return mGuestPenaltyCards;
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

    void addPenaltyCard(TeamType teamType, PenaltyCard penaltyCard) {
        if (TeamType.HOME.equals(teamType)) {
            mHomePenaltyCards.add(penaltyCard);
        } else {
            mGuestPenaltyCards.add(penaltyCard);
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

    boolean hasPenaltyCardEvents(TeamType teamType) {
        if (TeamType.HOME.equals(teamType)) {
            return mHomePenaltyCards.size() > 0;
        } else {
            return mGuestPenaltyCards.size() > 0;
        }
    }

    boolean hasEvent(TeamType teamType) {
        return hasSubstitutionEvents(teamType) || hasTimeoutEvents(teamType) || hasPenaltyCardEvents(teamType);
    }

    boolean hasSeveralEvents(TeamType teamType) {
        int count = 0;

        if (hasSubstitutionEvents(teamType)) {
            count++;
        }
        if (hasTimeoutEvents(teamType)) {
            count++;
        }
        if (hasPenaltyCardEvents(teamType)) {
            count++;
        }

        return count > 1;
    }
}
