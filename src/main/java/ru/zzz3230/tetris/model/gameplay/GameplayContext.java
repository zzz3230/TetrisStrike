package ru.zzz3230.tetris.model.gameplay;

import ru.zzz3230.tetris.model.gameplay.eventsData.BaseEventData;

public class GameplayContext {
    GameplayField gameplayField;
    GameplayEventType eventType;
    BaseEventData eventData;
    private final int score;
    public GameplayContext(GameplayField gameplayField, GameplayEventType eventType, BaseEventData eventData, int score) {
        this.gameplayField = gameplayField;
        this.eventType = eventType;
        this.eventData = eventData;
        this.score = score;
    }

    public GameplayField getGameplayField() {
        return gameplayField;
    }
    public GameplayEventType getEventType() {
        return eventType;
    }
    public BaseEventData getEventData() {
        return eventData;
    }
    public int getScore() {
        return score;
    }
}
