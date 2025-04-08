package ru.zzz3230.tetris.utils;

public abstract class Observable<C> {
    private Observer<C> observer;

    public void setObserver(Observer<C> observer) {
        this.observer = observer;
    }

    public void notifyObserver(C context) {
        if (observer != null) {
            observer.update(context);
        }
    }
}
