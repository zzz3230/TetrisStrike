package ru.zzz3230.tetris.model.gameplay.eventsData;

import ru.zzz3230.tetris.model.gameplay.GameplayEventType;

public record NotifyEventData(GameplayEventType type) implements BaseEventData{
    @Override
    public GameplayEventType getEventType() {
        return type;
    }
}
