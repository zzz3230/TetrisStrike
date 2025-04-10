package ru.zzz3230.tetris.model.gameplay.eventsData;

import ru.zzz3230.tetris.model.gameplay.GameplayEventType;

public record LinesClearedEventData(int[] indexes) implements BaseEventData{
    @Override
    public GameplayEventType getEventType() {
        return GameplayEventType.LINES_CLEARED;
    }
}
