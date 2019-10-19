package com.tonkar.volleyballreferee.engine.game;

import com.tonkar.volleyballreferee.engine.stored.api.ApiSanction;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSubstitution;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameEvent {

    public enum EventType {
        POINT, TIMEOUT, SUBSTITUTION, SANCTION
    }

    private final TeamType        teamType;
    private final EventType       eventType;
    private final ApiSubstitution substitution;
    private final ApiSanction     sanction;

    private GameEvent(TeamType teamType, EventType eventType, ApiSubstitution substitution, ApiSanction sanction) {
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

    public static GameEvent newSubstitutionEvent(TeamType teamType, ApiSubstitution substitution) {
        return new GameEvent(teamType, EventType.SUBSTITUTION, substitution, null);
    }

    public static GameEvent newSanctionEvent(TeamType teamType, ApiSanction sanction) {
        return new GameEvent(teamType, EventType.SANCTION, null, sanction);
    }

}