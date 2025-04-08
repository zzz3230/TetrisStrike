package ru.zzz3230.tblib.dto;

public record SentStatus(int code, String response) {

    @Override
    public String toString() {
        return "(" + code + ", " + response + ")";
    }
}
