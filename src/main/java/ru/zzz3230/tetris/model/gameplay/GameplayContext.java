package ru.zzz3230.tetris.model.gameplay;

import ru.zzz3230.tetris.model.gameplay.eventsData.BaseEventData;

public class GameplayContext {
    GameplayField gameplayField;
    BaseEventData eventData;
    private final int score;
    private final float iterationDelay;
    public GameplayContext(GameplayField gameplayField, BaseEventData eventData, int score, float iterationDelay) {
        this.gameplayField = gameplayField;
        this.eventData = eventData;
        this.score = score;
        this.iterationDelay = iterationDelay;
    }

    public GameplayField getGameplayField() {
        return gameplayField;
    }
    public BaseEventData getEventData() {
        return eventData;
    }
    public int getScore() {
        return score;
    }
    public float getIterationDelay() {
        return iterationDelay;
    }
}
