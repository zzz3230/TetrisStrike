package ru.zzz3230.tetris.dto;

import java.awt.*;

public class BlockData {
    private final boolean[][] form;
    private final Color color;

    private final int width;
    private final int height;

    public BlockData(int width, int height, boolean[][] form, Color color) {
        this.form = form;
        this.color = color;
        this.width = width;
        this.height = height;
    }

    public boolean[][] getForm() {
        return form;
    }
    public Color getColor() {
        return color;
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
}
