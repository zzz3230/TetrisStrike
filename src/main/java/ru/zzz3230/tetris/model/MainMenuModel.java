package ru.zzz3230.tetris.model;

import ru.zzz3230.tetris.utils.Observable;

import java.awt.*;

public class MainMenuModel extends Observable<MainMenuContext> {
    private String username;
    private Image avatar;

    public void setUsername(String username) {
        this.username = username;
        notifyObserver(new MainMenuContext(username, avatar));
    }

    public void setAvatar(Image avatar) {
        this.avatar = avatar;
        notifyObserver(new MainMenuContext(username, avatar));
    }
}
