package ru.zzz3230.tetris.model.gameplay;

import ru.zzz3230.tetris.dto.BlockData;
import ru.zzz3230.tetris.model.gameplay.eventsData.BaseEventData;

public record GameplayContext(GameplayField gameplayField, BaseEventData eventData, int score, float iterationDelay,
                              BlockData nextBlock) {
}
