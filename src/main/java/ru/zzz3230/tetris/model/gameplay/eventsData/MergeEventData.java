package ru.zzz3230.tetris.model.gameplay.eventsData;

import java.awt.*;

public record MergeEventData(Point mergePoint, Color fallingColor) implements BaseEventData{}
