package ru.zzz3230.tetris.dto;

import java.awt.*;

public record BlockData(int width, int height, boolean[][] form, Color color, float periodicRate) {
}