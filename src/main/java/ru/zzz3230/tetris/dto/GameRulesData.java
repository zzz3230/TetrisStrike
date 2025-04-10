package ru.zzz3230.tetris.dto;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GameRulesData {
    private List<BlockData> availableBlocks;

    public GameRulesData() {
        availableBlocks = Arrays.stream(new BlockData[]{
                new BlockData(3, 2, new boolean[][]{
                        {false, true, true},
                        {true, true, false}
                }, new Color(118, 34, 171), 0.85f),
                new BlockData(3, 2, new boolean[][]{
                        {true, true, false},
                        {false, true, true}
                }, new Color(83, 83, 83), 0.85f),
                new BlockData(3, 2, new boolean[][]{
                        {true, true, true},
                        {true, false, false}
                }, new Color(87, 171, 34), 0),
                new BlockData(3, 2, new boolean[][]{
                        {true, true, true},
                        {false, false, true}
                }, new Color(214, 72, 72), 0),
                new BlockData(3, 2, new boolean[][]{
                        {false, true, false},
                        {true, true, true}
                }, new Color(243, 150, 56), 1.15f),
                new BlockData(3, 2, new boolean[][]{
                        {true, true, false},
                        {true, true, false}
                }, new Color(56, 137, 242), 0),
                new BlockData(4, 1, new boolean[][]{
                        {true, true, true, true}
                }, Color.pink, 1.75f)
        }).toList();
    }

    public List<BlockData> getAvailableBlocks() {
        return availableBlocks;
    }
}
