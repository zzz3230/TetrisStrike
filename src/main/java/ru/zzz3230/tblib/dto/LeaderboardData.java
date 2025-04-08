package ru.zzz3230.tblib.dto;

public record LeaderboardData(LeaderboardEntry[] players) {
    public enum Scope {
        FRIENDS("friends"),
        GLOBAL("world");

        private final String scope;

        Scope(String scope) {
            this.scope = scope;
        }

        public String getScope() {
            return scope;
        }
    }
}
