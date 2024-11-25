package com.tonkar.volleyballreferee.engine.game;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import lombok.*;

@Getter
@Setter
public class GameEvent {

    public enum EventType {
        POINT,
        TIMEOUT,
        SUBSTITUTION,
        SANCTION
    }

    private final TeamType        teamType;
    private final EventType       eventType;
    private final SubstitutionDto substitution;
    private final SanctionDto     sanction;

    private GameEvent(TeamType teamType, EventType eventType, SubstitutionDto substitution, SanctionDto sanction) {
        this.teamType = teamType;
        this.eventType = eventType;
        this.substitution = substitution;
        this.sanction = sanction;
    }

    public static GameEvent newPointEvent(TeamType teamType) {
        return new GameEvent(teamType, EventType.POINT, null, null);
    }

    public static GameEvent newTimeoutEvent(TeamType teamType) {
        return new GameEvent(teamType, EventType.TIMEOUT, null, null);
    }

    public static GameEvent newSubstitutionEvent(TeamType teamType, SubstitutionDto substitution) {
        return new GameEvent(teamType, EventType.SUBSTITUTION, substitution, null);
    }

    public static GameEvent newSanctionEvent(TeamType teamType, SanctionDto sanction) {
        return new GameEvent(teamType, EventType.SANCTION, null, sanction);
    }

}