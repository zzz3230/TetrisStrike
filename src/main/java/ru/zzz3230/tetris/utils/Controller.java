package ru.zzz3230.tetris.utils;

public abstract class Controller {
    protected Page view;
    protected final NavigationManager navigationManager;
    protected boolean isActive;

    public Controller(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
    }

    public void showView(){
        navigationManager.navigateTo(view);
        isActive = true;
    }
    public void hideView(){
        isActive = false;
    }

    public boolean isActive(){
        return isActive;
    }
}
