package ru.zzz3230.tblib.dto;

import java.util.Date;

public record LeaderboardEntry(String username, int score, Date date, int aid) {
}
