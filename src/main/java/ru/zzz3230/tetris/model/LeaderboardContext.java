package ru.zzz3230.tetris.model;

import ru.zzz3230.tblib.dto.LeaderboardData;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public record LeaderboardContext(LeaderboardData data, Map<Integer, Image> avatars) {
}
